package es.delivery.manager.pedido.domain.event;

/**
 * Routing keys publicadas por pedido-service en delivery.exchange.
 */
public enum EventType {
    PEDIDO_CREADO("pedido.creado"),
    PEDIDO_ACEPTADO("pedido.aceptado"),
    PEDIDO_RECHAZADO("pedido.rechazado"),
    PEDIDO_ENTREGADO("pedido.entregado"),
    PEDIDO_CANCELADO("pedido.cancelado"),
    PAGO_SOLICITADO("pago.solicitado"),
    DEVOLUCION_SOLICITADA("devolucion.solicitada");

    private final String routingKey;

    EventType(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getRoutingKey() {
        return routingKey;
    }
}
