package es.delivery.manager.auth.infrastructure.security;

import es.delivery.manager.auth.domain.model.TokenClaims;
import es.delivery.manager.auth.domain.service.JwtPort;
import es.delivery.manager.contracts.model.TipoUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAdapter implements JwtPort {

    private final JwtProperties jwtProperties;

    @Override
    public String generate(TokenClaims claims) {
        return Jwts.builder()
                .subject(claims.getUserId())
                .claim("username", claims.getUsername())
                .claim("comercioId", claims.getComercioId())
                .claim("tipo", claims.getTipo().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                .signWith(secretKey())
                .compact();
    }

    @Override
    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(secretKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token invalido: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public TokenClaims extractClaims(String token) {
        Claims body = Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return TokenClaims.builder()
                .userId(body.getSubject())
                .username(body.get("username", String.class))
                .comercioId(body.get("comercioId", String.class))
                .tipo(TipoUsuario.valueOf(body.get("tipo", String.class)))
                .build();
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
