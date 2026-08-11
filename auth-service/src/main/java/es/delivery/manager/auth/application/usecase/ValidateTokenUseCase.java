package es.delivery.manager.auth.application.usecase;

import es.delivery.manager.auth.domain.model.TokenClaims;

public interface ValidateTokenUseCase {
    TokenClaims validate(String token);
}
