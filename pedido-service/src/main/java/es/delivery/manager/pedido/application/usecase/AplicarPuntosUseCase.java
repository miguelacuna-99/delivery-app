package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.TokenClaims;

import java.math.BigDecimal;

/**
 * Valida el saldo de puntos y los fija en el carrito. Excluyente con el cupon.
 */
public interface AplicarPuntosUseCase {
    BigDecimal aplicarPuntos(TokenClaims caller, int puntos);
}
