package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.Pedido;

/**
 * Reacciones a los eventos de pago-service (consumidos por el listener AMQP):
 * pago.completado -> PAGADO, pago.fallido -> CANCELADO,
 * devolucion.completada -> DEVUELTO.
 */
public interface RegistrarResultadoPagoUseCase {
    Pedido pagoCompletado(String pedidoId);
    Pedido pagoFallido(String pedidoId);
    Pedido devolucionCompletada(String pedidoId);
}
