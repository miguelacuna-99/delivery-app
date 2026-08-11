package es.delivery.manager.comercio.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductoMongoRepository extends MongoRepository<ProductoDocument, String> {
    List<ProductoDocument> findByComercioId(String comercioId);
}
