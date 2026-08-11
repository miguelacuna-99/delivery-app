package es.delivery.manager.fidelidad.application.usecase;

import es.delivery.manager.contracts.event.PedidoEventMessage;

/**
 * Reacciones a los eventos pedido.* (consumidos por el listener AMQP):
 * - pedido.creado: reserva los puntos aplicados y el uso del cupon
 * - pedido.aceptado: consolida la reserva
 * - pedido.rechazado: devuelve puntos y uso del cupon
 * - pedido.cancelado: idem (pago denegado o plazo de pago vencido)
 */
public interface ProcesarEventoPedidoUseCase {
    void pedidoCreado(PedidoEventMessage message);
    void pedidoAceptado(PedidoEventMessage message);
    void pedidoRechazado(PedidoEventMessage message);
    void pedidoCancelado(PedidoEventMessage message);
}
