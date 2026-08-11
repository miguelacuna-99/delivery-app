package es.delivery.manager.auth.infrastructure.repository;

import es.delivery.manager.auth.domain.model.PasswordResetToken;
import es.delivery.manager.auth.domain.repository.PasswordResetTokenRepository;
import es.delivery.manager.auth.infrastructure.mapper.PasswordResetTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

    private final PasswordResetTokenMongoRepository mongoRepository;
    private final PasswordResetTokenMapper tokenMapper;

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenDocument doc = tokenMapper.toDocument(token);
        return tokenMapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return mongoRepository.findByTokenHash(tokenHash).map(tokenMapper::toDomain);
    }

    @Override
    public void deleteByUserId(String userId) {
        mongoRepository.deleteByUserId(userId);
    }
}
