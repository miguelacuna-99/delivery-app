package es.delivery.manager.fidelidad.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Los endpoints internos (validar cupon, saldo de puntos) no los invoca un
 * usuario, sino pedido-service: no llevan JWT, sino una clave compartida de
 * servicio a servicio. Sin ella exponian el saldo de cualquier cliente cuyo
 * id se conociera.
 */
@Component
public class ServiceKeyInterceptor implements HandlerInterceptor {

    private static final String SERVICE_KEY_HEADER = "X-Service-Key";

    private final String serviceApiKey;

    public ServiceKeyInterceptor(@Value("${service.api-key}") String serviceApiKey) {
        this.serviceApiKey = serviceApiKey;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clave = request.getHeader(SERVICE_KEY_HEADER);
        if (!serviceApiKey.equals(clave)) {
            throw new UnauthorizedException("Se requiere una clave de servicio valida (" + SERVICE_KEY_HEADER + ")");
        }
        return true;
    }
}
