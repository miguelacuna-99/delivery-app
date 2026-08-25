package es.delivery.manager.pago.infrastructure.security;

import es.delivery.manager.pago.domain.model.TokenClaims;

/**
 * Claims del token de la peticion actual (ThreadLocal, poblado por JwtAuthInterceptor).
 */
public final class RequestSecurityContext {

    private static final ThreadLocal<TokenClaims> CLAIMS = new ThreadLocal<>();

    public static void set(TokenClaims claims) {
        CLAIMS.set(claims);
    }

    /** Claims de la peticion, o null si no llego token (endpoint publico). */
    public static TokenClaims get() {
        return CLAIMS.get();
    }

    /** Claims obligatorios: lanza UnauthorizedException si la peticion no traia token valido. */
    public static TokenClaims require() {
        TokenClaims claims = CLAIMS.get();
        if (claims == null) {
            throw new UnauthorizedException("Se requiere un token Bearer valido");
        }
        return claims;
    }

    public static void clear() {
        CLAIMS.remove();
    }

    private RequestSecurityContext() {
    }
}
