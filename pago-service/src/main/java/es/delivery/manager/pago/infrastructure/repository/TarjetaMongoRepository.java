package es.delivery.manager.pago.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TarjetaMongoRepository extends MongoRepository<TarjetaDocument, String> {
    List<TarjetaDocument> findByClienteId(String clienteId);
}
