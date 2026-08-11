package es.delivery.manager.pago.infrastructure.messaging;

import es.delivery.manager.contracts.event.PagoEventMessage;
import es.delivery.manager.contracts.event.RoutingKeys;
import es.delivery.manager.pago.domain.event.PagoEvent;
import es.delivery.manager.pago.domain.event.PagoEventPublisher;
import es.delivery.manager.pago.infrastructure.mapper.PagoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagoEventPublisherAdapter implements PagoEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final PagoMapper pagoMapper;

    @Override
    public void publish(PagoEvent event) {
        PagoEventMessage message = pagoMapper.toEventMessage(event.getPago());
        message.setRoutingKey(event.getRoutingKey());
        message.setTimestamp(event.getTimestamp());
        log.debug("Publicando {} para pedido {}", event.getRoutingKey(), message.getPedidoId());
        rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, event.getRoutingKey(), message);
    }
}
