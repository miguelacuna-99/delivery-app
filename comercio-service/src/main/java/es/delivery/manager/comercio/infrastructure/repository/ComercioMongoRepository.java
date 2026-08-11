package es.delivery.manager.comercio.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ComercioMongoRepository extends MongoRepository<ComercioDocument, String> {
    List<ComercioDocument> findByActivoTrue();
    boolean existsByCif(String cif);
}
