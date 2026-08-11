package es.delivery.manager.fidelidad.infrastructure.config;

import es.delivery.manager.fidelidad.infrastructure.security.JwtAuthInterceptor;
import es.delivery.manager.fidelidad.infrastructure.security.ServiceKeyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final ServiceKeyInterceptor serviceKeyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor).addPathPatterns("/api/**");

        // Endpoints internos: los llama pedido-service, no un usuario con JWT
        registry.addInterceptor(serviceKeyInterceptor)
                .addPathPatterns("/api/cupones/validar", "/api/puntos/*/saldo");
    }
}
