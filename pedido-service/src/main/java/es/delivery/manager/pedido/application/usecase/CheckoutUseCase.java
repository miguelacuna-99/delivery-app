package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.model.TokenClaims;

/**
 * Convierte el carrito del cliente en un Pedido PENDIENTE: snapshot de items,
 * calculo de importes (cupon y puntos validados contra fidelidad), numeroPedido
 * corto para el cliente y evento pedido.creado. El carrito se vacia.
 */
public interface CheckoutUseCase {
    Pedido checkout(TokenClaims caller);
}
