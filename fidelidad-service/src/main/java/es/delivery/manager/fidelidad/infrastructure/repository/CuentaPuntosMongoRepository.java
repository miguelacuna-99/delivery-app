package es.delivery.manager.fidelidad.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CuentaPuntosMongoRepository extends MongoRepository<CuentaPuntosDocument, String> {
    Optional<CuentaPuntosDocument> findByClienteIdAndComercioId(String clienteId, String comercioId);
}
