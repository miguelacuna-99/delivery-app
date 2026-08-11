package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.model.TokenClaims;

/**
 * El comercio acepta un pedido PENDIENTE fijando el tiempo estimado.
 * Publica pedido.aceptado y pago.solicitado.
 */
public interface AceptarPedidoUseCase {
    Pedido aceptar(TokenClaims caller, String pedidoId, int tiempoEstimadoMin);
}
