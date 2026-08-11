package es.delivery.manager.pedido.infrastructure.repository;

import es.delivery.manager.contracts.model.EstadoPedido;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PedidoMongoRepository extends MongoRepository<PedidoDocument, String> {
    Optional<PedidoDocument> findByNumeroPedido(String numeroPedido);
    List<PedidoDocument> findByClienteId(String clienteId);
    List<PedidoDocument> findByComercioIdAndEstado(String comercioId, EstadoPedido estado);
    List<PedidoDocument> findByEstadoAndFechaAceptacionBefore(EstadoPedido estado, Instant limite);
}
