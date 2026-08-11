package es.delivery.manager.pedido.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CarritoMongoRepository extends MongoRepository<CarritoDocument, String> {
    Optional<CarritoDocument> findByClienteId(String clienteId);
    void deleteByClienteId(String clienteId);
}
