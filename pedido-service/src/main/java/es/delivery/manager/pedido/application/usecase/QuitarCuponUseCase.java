package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.TokenClaims;

public interface QuitarCuponUseCase {
    void quitarCupon(TokenClaims caller);
}
