package es.delivery.manager.pedido.application.service;

import es.delivery.manager.pedido.application.usecase.CancelarPedidoUseCase;
import es.delivery.manager.pedido.application.usecase.RegistrarResultadoPagoUseCase;
import es.delivery.manager.pedido.domain.event.EventType;
import es.delivery.manager.pedido.domain.event.PedidoEvent;
import es.delivery.manager.pedido.domain.event.PedidoEventPublisher;
import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.repository.PedidoRepository;
import es.delivery.manager.contracts.model.EstadoPedido;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoResultadoService implements RegistrarResultadoPagoUseCase, CancelarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher eventPublisher;

    @Override
    public Pedido pagoCompletado(String pedidoId) {
        Pedido pedido = getPedido(pedidoId);
        if (pedido.getEstado() != EstadoPedido.ACEPTADO) {
            // Mensaje duplicado o fuera de orden: se ignora sin romper el consumo
            log.warn("pago.completado ignorado: pedido {} en estado {}", pedidoId, pedido.getEstado());
            return pedido;
        }
        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setFechaPago(Instant.now());
        return pedidoRepository.save(pedido);
    }

    @Override
    public Pedido pagoFallido(String pedidoId) {
        return cancelarPorFaltaDePago(pedidoId, "Pago denegado por la pasarela");
    }

    /**
     * Publica pedido.cancelado, que es lo que usa fidelidad para devolver la
     * reserva completa: ese mensaje lleva el codigo del cupon y los puntos
     * aplicados, cosa que pago.fallido no puede llevar.
     */
    @Override
    public Pedido cancelarPorFaltaDePago(String pedidoId, String motivo) {
        Pedido pedido = getPedido(pedidoId);
        if (pedido.getEstado() != EstadoPedido.ACEPTADO) {
            log.warn("Cancelacion ignorada: pedido {} en estado {}", pedidoId, pedido.getEstado());
            return pedido;
        }
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setMotivoCancelacion(motivo);
        pedido.setFechaCancelacion(Instant.now());
        Pedido saved = pedidoRepository.save(pedido);

        eventPublisher.publish(PedidoEvent.builder()
                .type(EventType.PEDIDO_CANCELADO)
                .pedido(saved)
                .timestamp(Instant.now())
                .build());
        return saved;
    }

    @Override
    public Pedido devolucionCompletada(String pedidoId) {
        Pedido pedido = getPedido(pedidoId);
        if (pedido.getEstado() != EstadoPedido.PENDIENTE_DEVOLUCION) {
            log.warn("devolucion.completada ignorada: pedido {} en estado {}", pedidoId, pedido.getEstado());
            return pedido;
        }
        pedido.setEstado(EstadoPedido.DEVUELTO);
        pedido.setFechaDevolucion(Instant.now());
        return pedidoRepository.save(pedido);
    }

    private Pedido getPedido(String pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new PedidoNotFoundException(pedidoId));
    }
}
