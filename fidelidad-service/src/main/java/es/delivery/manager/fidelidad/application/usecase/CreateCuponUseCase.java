package es.delivery.manager.fidelidad.application.usecase;

import es.delivery.manager.fidelidad.domain.model.Cupon;
import es.delivery.manager.fidelidad.domain.model.TokenClaims;

/**
 * Solo el ADMIN del comercio crea cupones.
 */
public interface CreateCuponUseCase {
    Cupon createCupon(TokenClaims caller, Cupon cupon);
}
