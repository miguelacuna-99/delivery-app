package es.delivery.manager.fidelidad.application.usecase;

import es.delivery.manager.contracts.event.PagoEventMessage;

/**
 * Reacciones a los eventos de pago-service (consumidos por el listener AMQP):
 * - pago.completado: otorga puntos por gasto (1 punto por euro del total)
 * - devolucion.completada: retira los puntos otorgados por el pedido devuelto
 *
 * El pago fallido NO se trata aqui: llega como pedido.cancelado, que si lleva
 * el cupon y los puntos necesarios para devolver la reserva entera.
 */
public interface ProcesarEventoPagoUseCase {
    void pagoCompletado(PagoEventMessage message);
    void devolucionCompletada(PagoEventMessage message);
}
