package es.delivery.manager.pedido.application.service;

import es.delivery.manager.pedido.application.usecase.*;
import es.delivery.manager.pedido.domain.event.EventType;
import es.delivery.manager.pedido.domain.event.PedidoEvent;
import es.delivery.manager.pedido.domain.event.PedidoEventPublisher;
import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.model.TokenClaims;
import es.delivery.manager.pedido.domain.repository.PedidoRepository;
import es.delivery.manager.contracts.model.EstadoPedido;
import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService implements ListPedidosClienteUseCase, ListPedidosComercioUseCase,
        AceptarPedidoUseCase, RechazarPedidoUseCase, EntregarPedidoUseCase, AnularPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher eventPublisher;

    @Override
    public List<Pedido> listPedidosCliente(TokenClaims caller) {
        CarritoService.checkCliente(caller);
        return pedidoRepository.findByClienteId(caller.getUserId());
    }

    @Override
    public List<Pedido> listPedidosComercio(TokenClaims caller, EstadoPedido estado) {
        checkUsuarioComercio(caller, TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL);
        return pedidoRepository.findByComercioIdAndEstado(caller.getComercioId(), estado);
    }

    @Override
    public Pedido aceptar(TokenClaims caller, String pedidoId, int tiempoEstimadoMin) {
        checkUsuarioComercio(caller, TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL);
        Pedido pedido = getPedidoDelComercio(caller, pedidoId);
        checkEstado(pedido, EstadoPedido.PENDIENTE, EstadoPedido.ACEPTADO);

        pedido.setEstado(EstadoPedido.ACEPTADO);
        pedido.setTiempoEstimadoMin(tiempoEstimadoMin);
        pedido.setFechaAceptacion(Instant.now());
        Pedido saved = pedidoRepository.save(pedido);

        publish(EventType.PEDIDO_ACEPTADO, saved);
        // El pago se solicita al aceptar: pago-service consume pago.solicitado
        publish(EventType.PAGO_SOLICITADO, saved);
        return saved;
    }

    @Override
    public Pedido rechazar(TokenClaims caller, String pedidoId, String mensaje) {
        checkUsuarioComercio(caller, TipoUsuario.ROOT, TipoUsuario.ADMIN, TipoUsuario.PERSONAL);
        Pedido pedido = getPedidoDelComercio(caller, pedidoId);
        checkEstado(pedido, EstadoPedido.PENDIENTE, EstadoPedido.RECHAZADO);

        pedido.setEstado(EstadoPedido.RECHAZADO);
        pedido.setMensajeComercio(mensaje);
        pedido.setFechaRechazo(Instant.now());
        Pedido saved = pedidoRepository.save(pedido);

        publish(EventType.PEDIDO_RECHAZADO, saved);
        return saved;
    }

    @Override
    public Pedido entregar(TokenClaims caller, String numeroPedido) {
        checkUsuarioComercio(caller, TipoUsuario.REPARTIDOR);
        Pedido pedido = pedidoRepository.findByNumeroPedido(numeroPedido)
                .filter(p -> p.getComercioId().equals(caller.getComercioId()))
                .orElseThrow(() -> new PedidoNotFoundException(numeroPedido));
        checkEstado(pedido, EstadoPedido.PAGADO, EstadoPedido.ENTREGADO);

        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setFechaEntrega(Instant.now());
        Pedido saved = pedidoRepository.save(pedido);

        publish(EventType.PEDIDO_ENTREGADO, saved);
        return saved;
    }

    @Override
    public Pedido anular(TokenClaims caller, String pedidoId, String motivo) {
        // Anular un pedido ya cobrado es decision de negocio: solo ROOT o ADMIN
        checkUsuarioComercio(caller, TipoUsuario.ROOT, TipoUsuario.ADMIN);
        Pedido pedido = getPedidoDelComercio(caller, pedidoId);
        checkEstado(pedido, EstadoPedido.PAGADO, EstadoPedido.PENDIENTE_DEVOLUCION);

        pedido.setEstado(EstadoPedido.PENDIENTE_DEVOLUCION);
        pedido.setMotivoAnulacion(motivo);
        pedido.setFechaAnulacion(Instant.now());
        Pedido saved = pedidoRepository.save(pedido);

        publish(EventType.DEVOLUCION_SOLICITADA, saved);
        return saved;
    }

    private Pedido getPedidoDelComercio(TokenClaims caller, String pedidoId) {
        // Multi-tenant: nunca operar sobre pedidos de otro comercio
        return pedidoRepository.findById(pedidoId)
                .filter(p -> p.getComercioId().equals(caller.getComercioId()))
                .orElseThrow(() -> new PedidoNotFoundException(pedidoId));
    }

    private void checkEstado(Pedido pedido, EstadoPedido esperado, EstadoPedido destino) {
        if (pedido.getEstado() != esperado) {
            throw new InvalidTransitionException(pedido.getEstado(), destino);
        }
    }

    private void checkUsuarioComercio(TokenClaims caller, TipoUsuario... permitidos) {
        for (TipoUsuario tipo : permitidos) {
            if (caller.getTipo() == tipo) {
                return;
            }
        }
        throw new ForbiddenOperationException(
                "El tipo " + caller.getTipo() + " no puede realizar esta operacion");
    }

    private void publish(EventType type, Pedido pedido) {
        eventPublisher.publish(PedidoEvent.builder()
                .type(type)
                .pedido(pedido)
                .timestamp(Instant.now())
                .build());
    }
}
