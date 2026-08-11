package es.delivery.manager.pedido.application.service;

import es.delivery.manager.contracts.model.EstadoPedido;
import es.delivery.manager.pedido.domain.event.EventType;
import es.delivery.manager.pedido.domain.event.PedidoEvent;
import es.delivery.manager.pedido.domain.event.PedidoEventPublisher;
import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoResultadoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoEventPublisher eventPublisher;

    @InjectMocks
    private PagoResultadoService pagoResultadoService;

    private Pedido pedido(EstadoPedido estado) {
        return Pedido.builder()
                .id("pedido-1")
                .comercioId("comercio-1")
                .clienteId("cliente-1")
                .codigoCupon("PROMO10")
                .puntosAplicados(100)
                .estado(estado)
                .build();
    }

    @Test
    void pagoCompletadoMarcaPagado() {
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido(EstadoPedido.ACEPTADO)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pagoResultadoService.pagoCompletado("pedido-1");

        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.PAGADO);
        assertThat(resultado.getFechaPago()).isNotNull();
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void pagoFallidoCancelaYPublicaPedidoCancelado() {
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido(EstadoPedido.ACEPTADO)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pagoResultadoService.pagoFallido("pedido-1");

        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(resultado.getFechaCancelacion()).isNotNull();
        assertThat(resultado.getMotivoCancelacion()).contains("denegado");

        // El evento lleva cupon y puntos: es lo que permite a fidelidad devolver la reserva entera
        ArgumentCaptor<PedidoEvent> captor = ArgumentCaptor.forClass(PedidoEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(EventType.PEDIDO_CANCELADO);
        assertThat(captor.getValue().getPedido().getCodigoCupon()).isEqualTo("PROMO10");
        assertThat(captor.getValue().getPedido().getPuntosAplicados()).isEqualTo(100);
    }

    @Test
    void cancelarPorPlazoVencidoTambienPublicaPedidoCancelado() {
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido(EstadoPedido.ACEPTADO)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pagoResultadoService.cancelarPorFaltaDePago("pedido-1", "Plazo de pago vencido");

        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(resultado.getMotivoCancelacion()).isEqualTo("Plazo de pago vencido");
        verify(eventPublisher).publish(any(PedidoEvent.class));
    }

    @Test
    void noSeCancelaUnPedidoYaPagado() {
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido(EstadoPedido.PAGADO)));

        Pedido resultado = pagoResultadoService.cancelarPorFaltaDePago("pedido-1", "Plazo vencido");

        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.PAGADO);
        verify(pedidoRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void devolucionCompletadaMarcaDevuelto() {
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido(EstadoPedido.PENDIENTE_DEVOLUCION)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pagoResultadoService.devolucionCompletada("pedido-1");

        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.DEVUELTO);
        assertThat(resultado.getFechaDevolucion()).isNotNull();
    }

    @Test
    void mensajesFueraDeOrdenSeIgnoranSinPersistir() {
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido(EstadoPedido.ENTREGADO)));

        Pedido resultado = pagoResultadoService.pagoCompletado("pedido-1");

        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.ENTREGADO);
        verify(pedidoRepository, never()).save(any());
    }
}
