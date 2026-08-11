package es.delivery.manager.pedido.domain.event;

/**
 * Puerto de publicacion de eventos. Adaptador: RabbitMQ (infrastructure/messaging).
 */
public interface PedidoEventPublisher {
    void publish(PedidoEvent event);
}
