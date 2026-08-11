package es.delivery.manager.pago.application.usecase;

import es.delivery.manager.pago.domain.model.Pago;

import java.math.BigDecimal;

/**
 * Procesa una solicitud de cobro (evento pago.solicitado): simula la pasarela
 * y publica pago.completado o pago.fallido.
 */
public interface ProcesarPagoUseCase {
    Pago procesarPago(String pedidoId, String comercioId, String clienteId, BigDecimal importe);
}
