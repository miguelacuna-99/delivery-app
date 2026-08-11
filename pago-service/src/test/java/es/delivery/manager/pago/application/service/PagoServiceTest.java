package es.delivery.manager.pago.application.service;

import es.delivery.manager.contracts.event.RoutingKeys;
import es.delivery.manager.contracts.model.EstadoPago;
import es.delivery.manager.pago.domain.event.PagoEvent;
import es.delivery.manager.pago.domain.event.PagoEventPublisher;
import es.delivery.manager.pago.domain.model.Pago;
import es.delivery.manager.pago.domain.repository.PagoRepository;
import es.delivery.manager.pago.domain.service.PasarelaPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PasarelaPort pasarelaPort;

    @Mock
    private PagoEventPublisher eventPublisher;

    @InjectMocks
    private PagoService pagoService;

    private static final BigDecimal IMPORTE = new BigDecimal("18.80");

    @Test
    void cobroAutorizadoPublicaPagoCompletado() {
        when(pagoRepository.findByPedidoId("pedido-1")).thenReturn(Optional.empty());
        when(pasarelaPort.cobrar("pedido-1", IMPORTE))
                .thenReturn(new PasarelaPort.ResultadoCobro(true, "firma-abc"));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Pago pago = pagoService.procesarPago("pedido-1", "comercio-1", "cliente-1", IMPORTE);

        assertThat(pago.getEstado()).isEqualTo(EstadoPago.COMPLETADO);
        assertThat(pago.getFirma()).isEqualTo("firma-abc");
        ArgumentCaptor<PagoEvent> captor = ArgumentCaptor.forClass(PagoEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getRoutingKey()).isEqualTo(RoutingKeys.PAGO_COMPLETADO);
    }

    @Test
    void cobroDenegadoPublicaPagoFallido() {
        when(pagoRepository.findByPedidoId("pedido-1")).thenReturn(Optional.empty());
        when(pasarelaPort.cobrar("pedido-1", IMPORTE))
                .thenReturn(new PasarelaPort.ResultadoCobro(false, "firma-abc"));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Pago pago = pagoService.procesarPago("pedido-1", "comercio-1", "cliente-1", IMPORTE);

        assertThat(pago.getEstado()).isEqualTo(EstadoPago.FALLIDO);
        ArgumentCaptor<PagoEvent> captor = ArgumentCaptor.forClass(PagoEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getRoutingKey()).isEqualTo(RoutingKeys.PAGO_FALLIDO);
    }

    @Test
    void pagoSolicitadoDuplicadoNoCobraDosVeces() {
        Pago existente = Pago.builder().pedidoId("pedido-1").estado(EstadoPago.COMPLETADO).build();
        when(pagoRepository.findByPedidoId("pedido-1")).thenReturn(Optional.of(existente));

        Pago pago = pagoService.procesarPago("pedido-1", "comercio-1", "cliente-1", IMPORTE);

        assertThat(pago).isSameAs(existente);
        verify(pasarelaPort, never()).cobrar(any(), any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void devolucionDePagoCompletadoPublicaDevolucionCompletada() {
        Pago existente = Pago.builder()
                .pedidoId("pedido-1")
                .estado(EstadoPago.COMPLETADO)
                .fecha(Instant.now())
                .build();
        when(pagoRepository.findByPedidoId("pedido-1")).thenReturn(Optional.of(existente));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Pago devuelto = pagoService.procesarDevolucion("pedido-1");

        assertThat(devuelto.getEstado()).isEqualTo(EstadoPago.DEVUELTO);
        assertThat(devuelto.getFechaDevolucion()).isNotNull();
        ArgumentCaptor<PagoEvent> captor = ArgumentCaptor.forClass(PagoEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getRoutingKey()).isEqualTo(RoutingKeys.DEVOLUCION_COMPLETADA);
    }

    @Test
    void devolucionDePagoNoCompletadoSeIgnora() {
        Pago fallido = Pago.builder().pedidoId("pedido-1").estado(EstadoPago.FALLIDO).build();
        when(pagoRepository.findByPedidoId("pedido-1")).thenReturn(Optional.of(fallido));

        Pago resultado = pagoService.procesarDevolucion("pedido-1");

        assertThat(resultado.getEstado()).isEqualTo(EstadoPago.FALLIDO);
        verify(pagoRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void devolucionSinPagoSeIgnora() {
        when(pagoRepository.findByPedidoId("pedido-1")).thenReturn(Optional.empty());

        Pago resultado = pagoService.procesarDevolucion("pedido-1");

        assertThat(resultado).isNull();
        verify(eventPublisher, never()).publish(any());
    }
}
