package es.delivery.manager.pedido.infrastructure.messaging;

import es.delivery.manager.contracts.event.PedidoEventMessage;
import es.delivery.manager.contracts.event.RoutingKeys;
import es.delivery.manager.pedido.domain.event.PedidoEvent;
import es.delivery.manager.pedido.domain.event.PedidoEventPublisher;
import es.delivery.manager.pedido.infrastructure.mapper.PedidoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoEventPublisherAdapter implements PedidoEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final PedidoMapper pedidoMapper;

    @Override
    public void publish(PedidoEvent event) {
        String routingKey = event.getType().getRoutingKey();
        PedidoEventMessage message = pedidoMapper.toEventMessage(event.getPedido());
        message.setRoutingKey(routingKey);
        message.setTimestamp(event.getTimestamp());
        log.debug("Publicando {} para pedido {}", routingKey, message.getPedidoId());
        rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, message);
    }
}
