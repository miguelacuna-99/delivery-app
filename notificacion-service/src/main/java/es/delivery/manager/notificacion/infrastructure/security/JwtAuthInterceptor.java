package es.delivery.manager.notificacion.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Si la peticion trae Authorization: Bearer, valida el token contra auth-service
 * y deja los claims en RequestSecurityContext. Un token invalido corta con 401;
 * la ausencia de token deja pasar (los endpoints protegidos exigen claims via
 * RequestSecurityContext.require()).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final AuthServiceClient authServiceClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return true;
        }
        String token = authorization.substring(7);
        return authServiceClient.validate(token)
                .map(claims -> {
                    RequestSecurityContext.set(claims);
                    return true;
                })
                .orElseThrow(() -> new UnauthorizedException("Token invalido o caducado"));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        RequestSecurityContext.clear();
    }
}
