package es.delivery.manager.pedido.domain.repository;

import es.delivery.manager.contracts.model.EstadoPedido;
import es.delivery.manager.pedido.domain.model.Pedido;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository {
    Pedido save(Pedido pedido);
    Optional<Pedido> findById(String id);
    Optional<Pedido> findByNumeroPedido(String numeroPedido);
    List<Pedido> findByClienteId(String clienteId);
    List<Pedido> findByComercioIdAndEstado(String comercioId, EstadoPedido estado);

    /** Pedidos atascados en un estado desde antes de un instante dado (plazo de pago vencido). */
    List<Pedido> findByEstadoAndFechaAceptacionBefore(EstadoPedido estado, Instant limite);
}
