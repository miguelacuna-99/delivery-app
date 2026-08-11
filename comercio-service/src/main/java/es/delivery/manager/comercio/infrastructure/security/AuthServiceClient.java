package es.delivery.manager.comercio.infrastructure.security;

import es.delivery.manager.comercio.domain.model.TokenClaims;
import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

/**
 * Valida tokens contra auth-service (POST /auth/validate).
 */
@Component
public class AuthServiceClient {

    private final RestClient restClient;

    public AuthServiceClient(@Value("${auth.service.url}") String authServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(authServiceUrl).build();
    }

    public Optional<TokenClaims> validate(String token) {
        ValidateResponse response = restClient.post()
                .uri("/auth/validate")
                .body(Map.of("token", token))
                .retrieve()
                .body(ValidateResponse.class);

        if (response == null || !response.isValid()) {
            return Optional.empty();
        }
        return Optional.of(TokenClaims.builder()
                .userId(response.getUserId())
                .username(response.getUsername())
                .comercioId(response.getComercioId())
                .tipo(response.getTipo())
                .build());
    }

    @Data
    static class ValidateResponse {
        private boolean valid;
        private String userId;
        private String username;
        private String comercioId;
        private TipoUsuario tipo;
    }
}
