package es.delivery.manager.pedido.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita @Scheduled. Aislado en su propia clase para que los tests de slice
 * web no arranquen el planificador.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
