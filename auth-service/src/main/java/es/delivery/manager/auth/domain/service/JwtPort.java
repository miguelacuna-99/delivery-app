package es.delivery.manager.auth.domain.service;

import es.delivery.manager.auth.domain.model.TokenClaims;

public interface JwtPort {
    String generate(TokenClaims claims);
    boolean isValid(String token);
    TokenClaims extractClaims(String token);
}
