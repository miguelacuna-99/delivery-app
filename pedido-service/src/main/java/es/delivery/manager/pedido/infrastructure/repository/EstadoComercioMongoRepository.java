package es.delivery.manager.pedido.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EstadoComercioMongoRepository extends MongoRepository<EstadoComercioDocument, String> {
    Optional<EstadoComercioDocument> findByComercioId(String comercioId);
}
