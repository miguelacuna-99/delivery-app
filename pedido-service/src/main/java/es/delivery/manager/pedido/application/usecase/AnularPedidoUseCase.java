package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.model.TokenClaims;

/**
 * El comercio anula un pedido PAGADO (PAGADO -> PENDIENTE_DEVOLUCION).
 * Publica devolucion.solicitada hacia el banco mock (pago-service).
 */
public interface AnularPedidoUseCase {
    Pedido anular(TokenClaims caller, String pedidoId, String motivo);
}
