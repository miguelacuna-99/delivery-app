package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.Carrito;
import es.delivery.manager.pedido.domain.model.TokenClaims;

/**
 * El cliente reemplaza el contenido de su carrito (items, cupon, puntos).
 * Un carrito es siempre sobre un unico comercio.
 */
public interface UpdateCarritoUseCase {
    Carrito updateCarrito(TokenClaims caller, Carrito carrito);
}
