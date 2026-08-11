package es.delivery.manager.contracts.event;

/**
 * Nombres del exchange y routing keys de RabbitMQ. Punto unico de verdad:
 * publicadores y consumidores usan estas constantes, nunca literales.
 */
public final class RoutingKeys {

    public static final String EXCHANGE = "delivery.exchange";

    public static final String PEDIDO_CREADO = "pedido.creado";
    public static final String PEDIDO_ACEPTADO = "pedido.aceptado";
    public static final String PEDIDO_RECHAZADO = "pedido.rechazado";
    public static final String PEDIDO_ENTREGADO = "pedido.entregado";
    /** El pedido no llego a cobrarse (pago denegado o plazo de pago vencido). */
    public static final String PEDIDO_CANCELADO = "pedido.cancelado";
    public static final String PAGO_SOLICITADO = "pago.solicitado";
    public static final String PAGO_COMPLETADO = "pago.completado";
    public static final String PAGO_FALLIDO = "pago.fallido";
    public static final String DEVOLUCION_SOLICITADA = "devolucion.solicitada";
    public static final String DEVOLUCION_COMPLETADA = "devolucion.completada";
    public static final String COMERCIO_SUSPENDIDO = "comercio.suspendido";
    public static final String COMERCIO_REACTIVADO = "comercio.reactivado";

    /**
     * Un binding wildcard solo es correcto si todas las claves que captura viajan
     * con el mismo tipo de mensaje.
     *
     * WILDCARD_PEDIDO es seguro: todas sus claves son PedidoEventMessage.
     *
     * WILDCARD_PAGO NO lo es: mezcla pago.solicitado (PedidoEventMessage) con
     * pago.completado / pago.fallido (PagoEventMessage). Una cola enlazada con el
     * comodin recibe los dos y falla al deserializar. Para consumir resultados de
     * pago, enlaza PAGO_COMPLETADO y PAGO_FALLIDO por separado.
     */
    public static final String WILDCARD_PEDIDO = "pedido.*";
    public static final String WILDCARD_PAGO = "pago.*";
    public static final String WILDCARD_DEVOLUCION = "devolucion.*";
    public static final String WILDCARD_COMERCIO = "comercio.*";

    private RoutingKeys() {
    }
}
