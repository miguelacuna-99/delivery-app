package es.delivery.manager.comercio.infrastructure.messaging;

import es.delivery.manager.comercio.domain.event.ComercioEvent;
import es.delivery.manager.comercio.domain.event.ComercioEventPublisher;
import es.delivery.manager.contracts.event.ComercioEventMessage;
import es.delivery.manager.contracts.event.RoutingKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ComercioEventPublisherAdapter implements ComercioEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(ComercioEvent event) {
        String routingKey = switch (event.getType()) {
            case SUSPENDIDO -> RoutingKeys.COMERCIO_SUSPENDIDO;
            case REACTIVADO -> RoutingKeys.COMERCIO_REACTIVADO;
        };
        ComercioEventMessage message = ComercioEventMessage.builder()
                .routingKey(routingKey)
                .comercioId(event.getComercio().getId())
                .nombre(event.getComercio().getNombre())
                .estadoSuscripcion(event.getComercio().getEstadoSuscripcion())
                .fechaFinSuscripcion(event.getComercio().getFechaFinSuscripcion())
                .timestamp(event.getTimestamp())
                .build();
        log.debug("Publicando {} para comercio {}", routingKey, message.getComercioId());
        rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, message);
    }
}
