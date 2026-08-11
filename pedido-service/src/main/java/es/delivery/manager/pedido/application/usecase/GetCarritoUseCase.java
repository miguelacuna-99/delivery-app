package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.Carrito;
import es.delivery.manager.pedido.domain.model.TokenClaims;

public interface GetCarritoUseCase {
    Carrito getCarrito(TokenClaims caller);
}
