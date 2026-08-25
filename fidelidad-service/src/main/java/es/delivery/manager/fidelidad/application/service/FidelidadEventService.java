package es.delivery.manager.fidelidad.application.service;

import es.delivery.manager.contracts.event.PagoEventMessage;
import es.delivery.manager.contracts.event.PedidoEventMessage;
import es.delivery.manager.fidelidad.application.usecase.ProcesarEventoPagoUseCase;
import es.delivery.manager.fidelidad.application.usecase.ProcesarEventoPedidoUseCase;
import es.delivery.manager.fidelidad.domain.model.*;
import es.delivery.manager.fidelidad.domain.repository.CuentaPuntosRepository;
import es.delivery.manager.fidelidad.domain.repository.CuponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;

/**
 * Ciclo reserva -> consolidacion de la fidelidad:
 * - pedido.creado: reserva (descuenta los puntos aplicados y cuenta el uso del cupon)
 * - pedido.aceptado: consolida (la reserva pasa a ser definitiva; sin efecto economico)
 * - pedido.rechazado / pedido.cancelado: devuelve puntos y uso del cupon
 * - pago.completado: otorga 1 punto por euro del total pagado
 * - devolucion.completada: retira los puntos otorgados por el pedido devuelto
 *
 * Todas las operaciones son idempotentes: RabbitMQ garantiza *al menos una*
 * entrega, asi que un evento repetido no puede abonar los puntos dos veces.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FidelidadEventService implements ProcesarEventoPedidoUseCase, ProcesarEventoPagoUseCase {

    private final CuentaPuntosRepository cuentaPuntosRepository;
    private final CuponRepository cuponRepository;

    @Override
    public void pedidoCreado(PedidoEventMessage message) {
        if (message.getPuntosAplicados() > 0) {
            registrarMovimiento(message.getClienteId(), message.getComercioId(), message.getPedidoId(),
                    TipoMovimiento.CANJEADO, -message.getPuntosAplicados());
        }
        if (tieneCupon(message)) {
            registrarUsoCupon(message, +1);
        }
    }

    @Override
    public void pedidoAceptado(PedidoEventMessage message) {
        // La reserva ya descontó puntos y conto el uso del cupon: consolidar
        // no tiene efecto economico, solo queda trazado en el log
        log.debug("Fidelidad consolidada para pedido {}", message.getPedidoId());
    }

    @Override
    public void pedidoRechazado(PedidoEventMessage message) {
        devolverReserva(message);
    }

    @Override
    public void pedidoCancelado(PedidoEventMessage message) {
        // Pago denegado o plazo de pago vencido: se libera todo lo reservado
        devolverReserva(message);
    }

    @Override
    public void pagoCompletado(PagoEventMessage message) {
        int puntos = message.getImporte() == null ? 0
                : message.getImporte().setScale(0, RoundingMode.FLOOR).intValue();
        if (puntos <= 0) {
            return;
        }
        if (yaHayMovimiento(message.getClienteId(), message.getComercioId(), message.getPedidoId(), TipoMovimiento.GANADO)) {
            log.debug("pago.completado repetido para pedido {}: los puntos ya se otorgaron",
                    message.getPedidoId());
            return;
        }
        registrarMovimiento(message.getClienteId(), message.getComercioId(), message.getPedidoId(),
                TipoMovimiento.GANADO, puntos);
    }

    @Override
    public void devolucionCompletada(PagoEventMessage message) {
        // Pedido DEVUELTO: se retiran los puntos que se otorgaron al pagarlo
        cuentaPuntosRepository.findByClienteIdAndComercioId(message.getClienteId(), message.getComercioId()).ifPresent(cuenta -> {
            int ganados = puntosNetosPorTipo(cuenta, message.getPedidoId(), TipoMovimiento.GANADO);
            int retirados = puntosNetosPorTipo(cuenta, message.getPedidoId(), TipoMovimiento.RETIRADO);
            int pendiente = ganados + retirados;
            if (pendiente > 0) {
                registrarMovimiento(message.getClienteId(), message.getComercioId(), message.getPedidoId(),
                        TipoMovimiento.RETIRADO, -pendiente);
            }
        });
    }

    /**
     * Devuelve los puntos y el uso del cupon reservados por el pedido. Solo actua
     * una vez: si ya hay un movimiento DEVUELTO para ese pedido, no repite nada.
     */
    private void devolverReserva(PedidoEventMessage message) {
        boolean yaDevuelto = yaHayMovimiento(message.getClienteId(), message.getComercioId(), message.getPedidoId(),
                TipoMovimiento.DEVUELTO);
        if (yaDevuelto) {
            log.debug("Reserva del pedido {} ya devuelta: se ignora el evento repetido",
                    message.getPedidoId());
            return;
        }
        if (message.getPuntosAplicados() > 0) {
            registrarMovimiento(message.getClienteId(), message.getComercioId(), message.getPedidoId(),
                    TipoMovimiento.DEVUELTO, message.getPuntosAplicados());
        }
        if (tieneCupon(message)) {
            registrarUsoCupon(message, -1);
        }
    }

    private boolean tieneCupon(PedidoEventMessage message) {
        return message.getCodigoCupon() != null && !message.getCodigoCupon().isBlank();
    }

    private boolean yaHayMovimiento(String clienteId, String comercioId, String pedidoId, TipoMovimiento tipo) {
        return cuentaPuntosRepository.findByClienteIdAndComercioId(clienteId, comercioId)
                .map(cuenta -> cuenta.getMovimientos() != null && cuenta.getMovimientos().stream()
                        .anyMatch(m -> pedidoId.equals(m.getPedidoId()) && m.getTipo() == tipo))
                .orElse(false);
    }

    private void registrarMovimiento(String clienteId, String comercioId, String pedidoId, TipoMovimiento tipo, int puntos) {
        CuentaPuntos cuenta = cuentaPuntosRepository.findByClienteIdAndComercioId(clienteId, comercioId)
                .orElseGet(() -> CuentaPuntos.builder()
                        .clienteId(clienteId)
                        .comercioId(comercioId)
                        .saldo(0)
                        .movimientos(new ArrayList<>())
                        .build());
        if (cuenta.getMovimientos() == null) {
            cuenta.setMovimientos(new ArrayList<>());
        }
        cuenta.setSaldo(cuenta.getSaldo() + puntos);
        cuenta.getMovimientos().add(MovimientoPuntos.builder()
                .pedidoId(pedidoId)
                .tipo(tipo)
                .puntos(puntos)
                .fecha(Instant.now())
                .build());
        cuentaPuntosRepository.save(cuenta);
        log.debug("Movimiento {} de {} puntos para cliente {} (pedido {})", tipo, puntos, clienteId, pedidoId);
    }

    private int puntosNetosPorTipo(CuentaPuntos cuenta, String pedidoId, TipoMovimiento tipo) {
        if (cuenta.getMovimientos() == null) {
            return 0;
        }
        return cuenta.getMovimientos().stream()
                .filter(m -> pedidoId.equals(m.getPedidoId()) && m.getTipo() == tipo)
                .mapToInt(MovimientoPuntos::getPuntos)
                .sum();
    }

    private void registrarUsoCupon(PedidoEventMessage message, int delta) {
        cuponRepository.findByCodigo(message.getCodigoCupon())
                .filter(c -> c.getComercioId().equals(message.getComercioId()))
                .ifPresentOrElse(cupon -> {
                    aplicarDeltaUso(cupon, message.getClienteId(), delta);
                    cuponRepository.save(cupon);
                }, () -> log.warn("Cupon {} no encontrado al procesar pedido {}",
                        message.getCodigoCupon(), message.getPedidoId()));
    }

    private void aplicarDeltaUso(Cupon cupon, String clienteId, int delta) {
        if (cupon.getUsos() == null) {
            cupon.setUsos(new ArrayList<>());
        }
        UsoCupon uso = CuponService.usosDe(cupon, clienteId).orElseGet(() -> {
            UsoCupon nuevo = UsoCupon.builder().clienteId(clienteId).contador(0).build();
            cupon.getUsos().add(nuevo);
            return nuevo;
        });
        uso.setContador(Math.max(0, uso.getContador() + delta));
    }
}
