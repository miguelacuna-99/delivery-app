package es.delivery.manager.pago.application.usecase;

import es.delivery.manager.pago.domain.model.Pago;

/**
 * Procesa una devolucion (evento devolucion.solicitada): simula el ingreso
 * del banco al cliente y publica devolucion.completada.
 */
public interface ProcesarDevolucionUseCase {
    Pago procesarDevolucion(String pedidoId);
}
