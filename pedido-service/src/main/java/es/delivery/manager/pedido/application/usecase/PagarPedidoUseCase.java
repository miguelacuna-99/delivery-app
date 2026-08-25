package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.model.TokenClaims;

/**
 * El cliente dispara el cobro de su propio pedido ACEPTADO, eligiendo la
 * tarjeta con la que paga. El cobro en si sigue siendo asincrono via eventos.
 */
public interface PagarPedidoUseCase {
    Pedido pagar(TokenClaims caller, String pedidoId, String tarjetaId);
}
