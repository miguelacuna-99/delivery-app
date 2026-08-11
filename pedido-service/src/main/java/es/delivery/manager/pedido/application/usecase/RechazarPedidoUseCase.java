package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.model.TokenClaims;

/**
 * El comercio rechaza un pedido PENDIENTE con mensaje opcional.
 * Publica pedido.rechazado (fidelidad devuelve reserva de puntos/cupon).
 */
public interface RechazarPedidoUseCase {
    Pedido rechazar(TokenClaims caller, String pedidoId, String mensaje);
}
