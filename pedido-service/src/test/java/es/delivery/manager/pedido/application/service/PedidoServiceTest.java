package es.delivery.manager.pedido.application.service;

import es.delivery.manager.contracts.model.EstadoPedido;
import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.pedido.domain.event.EventType;
import es.delivery.manager.pedido.domain.event.PedidoEvent;
import es.delivery.manager.pedido.domain.event.PedidoEventPublisher;
import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.model.TokenClaims;
import es.delivery.manager.pedido.domain.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoEventPublisher eventPublisher;

    @InjectMocks
    private PedidoService pedidoService;

    private TokenClaims caller(TipoUsuario tipo) {
        return TokenClaims.builder()
                .userId("user-1")
                .username("user")
                .comercioId("comercio-1")
                .tipo(tipo)
                .build();
    }

    private Pedido pedido(EstadoPedido estado) {
        return Pedido.builder()
                .id("pedido-1")
                .numeroPedido("ABC234")
                .comercioId("comercio-1")
                .clienteId("cliente-1")
                .estado(estado)
                .build();
    }

    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"ROOT", "ADMIN", "PERSONAL"})
    void usuariosDelComercioAceptanPedidoPendiente(TipoUsuario tipo) {
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido(EstadoPedido.PENDIENTE)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido aceptado = pedidoService.aceptar(caller(tipo), "pedido-1", 30);

        assertThat(aceptado.getEstado()).isEqualTo(EstadoPedido.ACEPTADO);
        assertThat(aceptado.getTiempoEstimadoMin()).isEqualTo(30);
        assertThat(aceptado.getFechaAceptacion()).isNotNull();

        // Aceptar ya no dispara el cobro: eso lo hace el cliente via pagar()
        ArgumentCaptor<PedidoEvent> captor = ArgumentCaptor.forClass(PedidoEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(EventType.PEDIDO_ACEPTADO);
    }

    @Test
    void noSePuedeAceptarUnPedidoYaAceptado() {
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido(EstadoPedido.ACEPTADO)));

        assertThatThrownBy(() -> pedidoService.aceptar(caller(TipoUsuario.ROOT), "pedido-1", 30))
                .isInstanceOf(InvalidTransitionException.class);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void noSePuedeOperarSobrePedidosDeOtroComercio() {
        Pedido ajeno = pedido(EstadoPedido.PENDIENTE);
        ajeno.setComercioId("comercio-2");
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(ajeno));

        assertThatThrownBy(() -> pedidoService.aceptar(caller(TipoUsuario.ROOT), "pedido-1", 30))
                .isInstanceOf(PedidoNotFoundException.class);
    }

    @Test
    void rechazarGuardaMensajeYPublicaPedidoRechazado() {
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido(EstadoPedido.PENDIENTE)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido rechazado = pedidoService.rechazar(caller(TipoUsuario.PERSONAL), "pedido-1", "Sin stock");

        assertThat(rechazado.getEstado()).isEqualTo(EstadoPedido.RECHAZADO);
        assertThat(rechazado.getMensajeComercio()).isEqualTo("Sin stock");
        ArgumentCaptor<PedidoEvent> captor = ArgumentCaptor.forClass(PedidoEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(EventType.PEDIDO_RECHAZADO);
    }

    @Test
    void elRepartidorEntregaPorNumeroPedido() {
        when(pedidoRepository.findByNumeroPedido("ABC234")).thenReturn(Optional.of(pedido(EstadoPedido.PAGADO)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido entregado = pedidoService.entregar(caller(TipoUsuario.REPARTIDOR), "ABC234");

        assertThat(entregado.getEstado()).isEqualTo(EstadoPedido.ENTREGADO);
        assertThat(entregado.getFechaEntrega()).isNotNull();
        ArgumentCaptor<PedidoEvent> captor = ArgumentCaptor.forClass(PedidoEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(EventType.PEDIDO_ENTREGADO);
    }

    @Test
    void soloElRepartidorEntrega() {
        assertThatThrownBy(() -> pedidoService.entregar(caller(TipoUsuario.PERSONAL), "ABC234"))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void noSeEntregaUnPedidoSinPagar() {
        when(pedidoRepository.findByNumeroPedido("ABC234")).thenReturn(Optional.of(pedido(EstadoPedido.ACEPTADO)));

        assertThatThrownBy(() -> pedidoService.entregar(caller(TipoUsuario.REPARTIDOR), "ABC234"))
                .isInstanceOf(InvalidTransitionException.class);
    }

    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"ROOT", "ADMIN"})
    void rootYAdminAnulanPedidoPagado(TipoUsuario tipo) {
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido(EstadoPedido.PAGADO)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido anulado = pedidoService.anular(caller(tipo), "pedido-1", "Cliente reclama");

        assertThat(anulado.getEstado()).isEqualTo(EstadoPedido.PENDIENTE_DEVOLUCION);
        assertThat(anulado.getMotivoAnulacion()).isEqualTo("Cliente reclama");
        ArgumentCaptor<PedidoEvent> captor = ArgumentCaptor.forClass(PedidoEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(EventType.DEVOLUCION_SOLICITADA);
    }

    @Test
    void personalNoPuedeAnular() {
        assertThatThrownBy(() -> pedidoService.anular(caller(TipoUsuario.PERSONAL), "pedido-1", "x"))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void soloSeAnulanPedidosPagados() {
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido(EstadoPedido.PENDIENTE)));

        assertThatThrownBy(() -> pedidoService.anular(caller(TipoUsuario.ROOT), "pedido-1", "x"))
                .isInstanceOf(InvalidTransitionException.class);
    }

    private TokenClaims cliente() {
        return TokenClaims.builder().userId("cliente-1").username("ana").tipo(TipoUsuario.CLIENTE).build();
    }

    @Test
    void elClientePagaSuPedidoAceptadoYDisparaElCobro() {
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido(EstadoPedido.ACEPTADO)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido pagado = pedidoService.pagar(cliente(), "pedido-1", "tarjeta-1");

        assertThat(pagado.getTarjetaId()).isEqualTo("tarjeta-1");
        ArgumentCaptor<PedidoEvent> captor = ArgumentCaptor.forClass(PedidoEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(EventType.PAGO_SOLICITADO);
    }

    @Test
    void pagarSinTarjetaLanza400() {
        assertThatThrownBy(() -> pedidoService.pagar(cliente(), "pedido-1", ""))
                .isInstanceOf(TarjetaRequeridaException.class);

        verify(pedidoRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void noSePuedePagarUnPedidoDeOtroCliente() {
        Pedido ajeno = pedido(EstadoPedido.ACEPTADO);
        ajeno.setClienteId("otro-cliente");
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(ajeno));

        assertThatThrownBy(() -> pedidoService.pagar(cliente(), "pedido-1", "tarjeta-1"))
                .isInstanceOf(PedidoNotFoundException.class);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void noSePuedePagarUnPedidoQueNoEstaAceptado() {
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido(EstadoPedido.PENDIENTE)));

        assertThatThrownBy(() -> pedidoService.pagar(cliente(), "pedido-1", "tarjeta-1"))
                .isInstanceOf(InvalidTransitionException.class);

        verify(eventPublisher, never()).publish(any());
    }
}
