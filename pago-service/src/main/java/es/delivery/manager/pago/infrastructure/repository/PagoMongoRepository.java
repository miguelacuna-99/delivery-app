package es.delivery.manager.pago.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PagoMongoRepository extends MongoRepository<PagoDocument, String> {
    Optional<PagoDocument> findByPedidoId(String pedidoId);
    List<PagoDocument> findByClienteId(String clienteId);
}
