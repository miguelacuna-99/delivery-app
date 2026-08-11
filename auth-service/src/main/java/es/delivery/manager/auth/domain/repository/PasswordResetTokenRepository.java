package es.delivery.manager.auth.domain.repository;

import es.delivery.manager.auth.domain.model.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository {
    PasswordResetToken save(PasswordResetToken token);
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    void deleteByUserId(String userId);
}
