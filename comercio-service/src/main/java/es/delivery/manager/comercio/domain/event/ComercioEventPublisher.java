package es.delivery.manager.comercio.domain.event;

/**
 * Puerto de publicacion de eventos. Adaptador: RabbitMQ (infrastructure/messaging).
 */
public interface ComercioEventPublisher {
    void publish(ComercioEvent event);
}
