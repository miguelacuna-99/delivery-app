package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.TokenClaims;

import java.math.BigDecimal;

/**
 * Valida el cupon contra fidelidad-service en el momento de aplicarlo (no solo
 * al final del checkout) y lo deja fijado en el carrito si es usable.
 */
public interface AplicarCuponUseCase {
    BigDecimal aplicarCupon(TokenClaims caller, String codigo);
}
