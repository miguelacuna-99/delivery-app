package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.TokenClaims;

public interface ClearCarritoUseCase {
    void clearCarrito(TokenClaims caller);
}
