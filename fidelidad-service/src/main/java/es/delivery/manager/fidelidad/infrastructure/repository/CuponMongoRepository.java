package es.delivery.manager.fidelidad.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CuponMongoRepository extends MongoRepository<CuponDocument, String> {
    Optional<CuponDocument> findByCodigo(String codigo);
    List<CuponDocument> findByComercioId(String comercioId);
}
