package es.delivery.manager.auth.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioMongoRepository extends MongoRepository<UsuarioDocument, String> {
    Optional<UsuarioDocument> findByUsername(String username);
    Optional<UsuarioDocument> findByMail(String mail);
    boolean existsByUsername(String username);
    List<UsuarioDocument> findByComercioId(String comercioId);
}
