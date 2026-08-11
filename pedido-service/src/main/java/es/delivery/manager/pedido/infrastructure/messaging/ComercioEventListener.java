package es.delivery.manager.pedido.infrastructure.messaging;

import es.delivery.manager.contracts.event.ComercioEventMessage;
import es.delivery.manager.contracts.event.RoutingKeys;
import es.delivery.manager.pedido.application.usecase.ActualizarEstadoComercioUseCase;
import es.delivery.manager.pedido.infrastructure.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Un comercio con la suscripcion suspendida deja de admitir pedidos nuevos.
 * Se guarda una replica local del estado para no tener que preguntar a
 * comercio-service en cada checkout.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ComercioEventListener {

    private final ActualizarEstadoComercioUseCase actualizarEstadoComercioUseCase;

    @RabbitListener(queues = RabbitConfig.COMERCIO_QUEUE)
    public void onComercioEvent(ComercioEventMessage message) {
        log.debug("Recibido {} para comercio {}", message.getRoutingKey(), message.getComercioId());
        switch (message.getRoutingKey()) {
            case RoutingKeys.COMERCIO_SUSPENDIDO ->
                    actualizarEstadoComercioUseCase.marcarSuspendido(message.getComercioId());
            case RoutingKeys.COMERCIO_REACTIVADO ->
                    actualizarEstadoComercioUseCase.marcarOperativo(message.getComercioId());
            default -> log.warn("Routing key inesperada en la cola de comercio: {}", message.getRoutingKey());
        }
    }
}
