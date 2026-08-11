package es.delivery.manager.fidelidad.application.service;

import es.delivery.manager.contracts.event.PagoEventMessage;
import es.delivery.manager.contracts.event.PedidoEventMessage;
import es.delivery.manager.fidelidad.domain.model.*;
import es.delivery.manager.fidelidad.domain.repository.CuentaPuntosRepository;
import es.delivery.manager.fidelidad.domain.repository.CuponRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FidelidadEventServiceTest {

    @Mock
    private CuentaPuntosRepository cuentaPuntosRepository;

    @Mock
    private CuponRepository cuponRepository;

    @InjectMocks
    private FidelidadEventService fidelidadEventService;

    private CuentaPuntos cuenta(int saldo, List<MovimientoPuntos> movimientos) {
        return CuentaPuntos.builder()
                .clienteId("cliente-1")
                .saldo(saldo)
                .movimientos(new ArrayList<>(movimientos))
                .build();
    }

    private MovimientoPuntos movimiento(TipoMovimiento tipo, int puntos) {
        return MovimientoPuntos.builder()
                .pedidoId("pedido-1").tipo(tipo).puntos(puntos).fecha(Instant.now())
                .build();
    }

    private PedidoEventMessage pedidoMessage(int puntos, String cupon) {
        return PedidoEventMessage.builder()
                .pedidoId("pedido-1")
                .comercioId("comercio-1")
                .clienteId("cliente-1")
                .puntosAplicados(puntos)
                .codigoCupon(cupon)
                .build();
    }

    private PagoEventMessage pagoMessage(BigDecimal importe) {
        return PagoEventMessage.builder()
                .pedidoId("pedido-1")
                .comercioId("comercio-1")
                .clienteId("cliente-1")
                .importe(importe)
                .build();
    }

    private Cupon cuponCon(int contadorDeUsos) {
        List<UsoCupon> usos = contadorDeUsos < 0
                ? new ArrayList<>()
                : new ArrayList<>(List.of(UsoCupon.builder().clienteId("cliente-1").contador(contadorDeUsos).build()));
        return Cupon.builder()
                .comercioId("comercio-1").codigo("PROMO10")
                .usosMaximosPorUsuario(2).usos(usos)
                .estado(EstadoCupon.ACTIVO)
                .build();
    }

    @Test
    void pedidoCreadoReservaPuntosYUsoDeCupon() {
        when(cuentaPuntosRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(cuenta(200, List.of())));
        when(cuentaPuntosRepository.save(any(CuentaPuntos.class))).thenAnswer(inv -> inv.getArgument(0));
        Cupon cupon = cuponCon(-1);
        when(cuponRepository.findByCodigo("PROMO10")).thenReturn(Optional.of(cupon));
        when(cuponRepository.save(any(Cupon.class))).thenAnswer(inv -> inv.getArgument(0));

        fidelidadEventService.pedidoCreado(pedidoMessage(100, "PROMO10"));

        ArgumentCaptor<CuentaPuntos> captor = ArgumentCaptor.forClass(CuentaPuntos.class);
        verify(cuentaPuntosRepository).save(captor.capture());
        assertThat(captor.getValue().getSaldo()).isEqualTo(100);
        assertThat(captor.getValue().getMovimientos())
                .anyMatch(m -> m.getTipo() == TipoMovimiento.CANJEADO && m.getPuntos() == -100);
        assertThat(cupon.getUsos()).anyMatch(u -> u.getClienteId().equals("cliente-1") && u.getContador() == 1);
    }

    @Test
    void pedidoRechazadoDevuelvePuntosYUsoDeCupon() {
        when(cuentaPuntosRepository.findByClienteId("cliente-1"))
                .thenReturn(Optional.of(cuenta(100, List.of(movimiento(TipoMovimiento.CANJEADO, -100)))));
        when(cuentaPuntosRepository.save(any(CuentaPuntos.class))).thenAnswer(inv -> inv.getArgument(0));
        Cupon cupon = cuponCon(1);
        when(cuponRepository.findByCodigo("PROMO10")).thenReturn(Optional.of(cupon));
        when(cuponRepository.save(any(Cupon.class))).thenAnswer(inv -> inv.getArgument(0));

        fidelidadEventService.pedidoRechazado(pedidoMessage(100, "PROMO10"));

        ArgumentCaptor<CuentaPuntos> captor = ArgumentCaptor.forClass(CuentaPuntos.class);
        verify(cuentaPuntosRepository).save(captor.capture());
        assertThat(captor.getValue().getSaldo()).isEqualTo(200);
        assertThat(cupon.getUsos().get(0).getContador()).isZero();
    }

    @Test
    void pedidoCanceladoDevuelvePuntosYLiberaElCupon() {
        when(cuentaPuntosRepository.findByClienteId("cliente-1"))
                .thenReturn(Optional.of(cuenta(100, List.of(movimiento(TipoMovimiento.CANJEADO, -100)))));
        when(cuentaPuntosRepository.save(any(CuentaPuntos.class))).thenAnswer(inv -> inv.getArgument(0));
        Cupon cupon = cuponCon(1);
        when(cuponRepository.findByCodigo("PROMO10")).thenReturn(Optional.of(cupon));
        when(cuponRepository.save(any(Cupon.class))).thenAnswer(inv -> inv.getArgument(0));

        fidelidadEventService.pedidoCancelado(pedidoMessage(100, "PROMO10"));

        ArgumentCaptor<CuentaPuntos> captor = ArgumentCaptor.forClass(CuentaPuntos.class);
        verify(cuentaPuntosRepository).save(captor.capture());
        assertThat(captor.getValue().getSaldo()).isEqualTo(200);
        // Lo que antes no ocurria con pago.fallido: el uso del cupon vuelve a estar libre
        assertThat(cupon.getUsos().get(0).getContador()).isZero();
    }

    @Test
    void unaCancelacionRepetidaNoDevuelveLosPuntosDosVeces() {
        when(cuentaPuntosRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(cuenta(200, List.of(
                movimiento(TipoMovimiento.CANJEADO, -100),
                movimiento(TipoMovimiento.DEVUELTO, 100)))));

        fidelidadEventService.pedidoCancelado(pedidoMessage(100, "PROMO10"));

        verify(cuentaPuntosRepository, never()).save(any());
        verify(cuponRepository, never()).save(any());
    }

    @Test
    void pagoCompletadoOtorgaUnPuntoPorEuro() {
        when(cuentaPuntosRepository.findByClienteId("cliente-1")).thenReturn(Optional.empty());
        when(cuentaPuntosRepository.save(any(CuentaPuntos.class))).thenAnswer(inv -> inv.getArgument(0));

        fidelidadEventService.pagoCompletado(pagoMessage(new BigDecimal("18.80")));

        ArgumentCaptor<CuentaPuntos> captor = ArgumentCaptor.forClass(CuentaPuntos.class);
        verify(cuentaPuntosRepository).save(captor.capture());
        assertThat(captor.getValue().getSaldo()).isEqualTo(18);
        assertThat(captor.getValue().getMovimientos())
                .anyMatch(m -> m.getTipo() == TipoMovimiento.GANADO && m.getPuntos() == 18);
    }

    @Test
    void unPagoCompletadoRepetidoNoOtorgaPuntosDosVeces() {
        when(cuentaPuntosRepository.findByClienteId("cliente-1"))
                .thenReturn(Optional.of(cuenta(18, List.of(movimiento(TipoMovimiento.GANADO, 18)))));

        fidelidadEventService.pagoCompletado(pagoMessage(new BigDecimal("18.80")));

        verify(cuentaPuntosRepository, never()).save(any());
    }

    @Test
    void devolucionCompletadaRetiraLosPuntosGanados() {
        when(cuentaPuntosRepository.findByClienteId("cliente-1"))
                .thenReturn(Optional.of(cuenta(18, List.of(movimiento(TipoMovimiento.GANADO, 18)))));
        when(cuentaPuntosRepository.save(any(CuentaPuntos.class))).thenAnswer(inv -> inv.getArgument(0));

        fidelidadEventService.devolucionCompletada(pagoMessage(new BigDecimal("18.80")));

        ArgumentCaptor<CuentaPuntos> captor = ArgumentCaptor.forClass(CuentaPuntos.class);
        verify(cuentaPuntosRepository).save(captor.capture());
        assertThat(captor.getValue().getSaldo()).isZero();
        assertThat(captor.getValue().getMovimientos())
                .anyMatch(m -> m.getTipo() == TipoMovimiento.RETIRADO && m.getPuntos() == -18);
    }

    @Test
    void devolucionCompletadaEsIdempotente() {
        when(cuentaPuntosRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(cuenta(0, List.of(
                movimiento(TipoMovimiento.GANADO, 18),
                movimiento(TipoMovimiento.RETIRADO, -18)))));

        fidelidadEventService.devolucionCompletada(pagoMessage(new BigDecimal("18.80")));

        verify(cuentaPuntosRepository, never()).save(any());
    }
}
