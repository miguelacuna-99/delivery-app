package es.delivery.manager.auth.infrastructure.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PasswordResetTokenMongoRepository extends MongoRepository<PasswordResetTokenDocument, String> {
    Optional<PasswordResetTokenDocument> findByTokenHash(String tokenHash);
    void deleteByUserId(String userId);
}
