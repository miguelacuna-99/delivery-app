package es.delivery.manager.pago.domain.event;

public interface PagoEventPublisher {
    void publish(PagoEvent event);
}
