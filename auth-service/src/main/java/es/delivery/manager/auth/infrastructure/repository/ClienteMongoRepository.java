package es.delivery.manager.auth.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ClienteMongoRepository extends MongoRepository<ClienteDocument, String> {
    Optional<ClienteDocument> findByUsername(String username);
    Optional<ClienteDocument> findByMail(String mail);
    boolean existsByUsername(String username);
}
