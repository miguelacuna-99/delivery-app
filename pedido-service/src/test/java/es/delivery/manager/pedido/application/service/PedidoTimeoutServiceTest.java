package es.delivery.manager.pedido.application.service;

import es.delivery.manager.contracts.model.EstadoPedido;
import es.delivery.manager.pedido.application.usecase.CancelarPedidoUseCase;
import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoTimeoutServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private CancelarPedidoUseCase cancelarPedidoUseCase;

    private PedidoTimeoutService servicioCon(long timeoutMin) {
        return new PedidoTimeoutService(pedidoRepository, cancelarPedidoUseCase, timeoutMin);
    }

    private Pedido pedido(String id) {
        return Pedido.builder()
                .id(id)
                .comercioId("comercio-1")
                .estado(EstadoPedido.ACEPTADO)
                .fechaAceptacion(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();
    }

    @Test
    void cancelaLosPedidosConElPlazoVencido() {
        when(pedidoRepository.findByEstadoAndFechaAceptacionBefore(eq(EstadoPedido.ACEPTADO), any()))
                .thenReturn(List.of(pedido("pedido-1"), pedido("pedido-2")));

        int cancelados = servicioCon(15).cancelarExpirados();

        assertThat(cancelados).isEqualTo(2);
        verify(cancelarPedidoUseCase).cancelarPorFaltaDePago(eq("pedido-1"), anyString());
        verify(cancelarPedidoUseCase).cancelarPorFaltaDePago(eq("pedido-2"), anyString());
    }

    @Test
    void buscaSoloAceptadosAnterioresAlPlazo() {
        when(pedidoRepository.findByEstadoAndFechaAceptacionBefore(eq(EstadoPedido.ACEPTADO), any()))
                .thenReturn(List.of());

        servicioCon(30).cancelarExpirados();

        ArgumentCaptor<Instant> limite = ArgumentCaptor.forClass(Instant.class);
        verify(pedidoRepository).findByEstadoAndFechaAceptacionBefore(eq(EstadoPedido.ACEPTADO), limite.capture());
        // El limite es "hace 30 minutos": nada mas reciente se toca
        assertThat(limite.getValue()).isBefore(Instant.now().minus(29, ChronoUnit.MINUTES));
        assertThat(limite.getValue()).isAfter(Instant.now().minus(31, ChronoUnit.MINUTES));
    }

    @Test
    void sinPedidosExpiradosNoHaceNada() {
        when(pedidoRepository.findByEstadoAndFechaAceptacionBefore(eq(EstadoPedido.ACEPTADO), any()))
                .thenReturn(List.of());

        assertThat(servicioCon(15).cancelarExpirados()).isZero();
        verifyNoInteractions(cancelarPedidoUseCase);
    }

    @Test
    void unFalloAisladoNoDetieneElBarrido() {
        when(pedidoRepository.findByEstadoAndFechaAceptacionBefore(eq(EstadoPedido.ACEPTADO), any()))
                .thenReturn(List.of(pedido("malo"), pedido("bueno")));
        doThrow(new IllegalStateException("boom"))
                .when(cancelarPedidoUseCase).cancelarPorFaltaDePago(eq("malo"), anyString());

        int cancelados = servicioCon(15).cancelarExpirados();

        assertThat(cancelados).isEqualTo(1);
        verify(cancelarPedidoUseCase).cancelarPorFaltaDePago(eq("bueno"), anyString());
    }
}
