package es.delivery.manager.fidelidad.application.usecase;

import es.delivery.manager.fidelidad.domain.model.Cupon;
import es.delivery.manager.fidelidad.domain.model.TokenClaims;

/**
 * Solo el ADMIN del comercio anula cupones (estado ANULADO).
 */
public interface AnularCuponUseCase {
    Cupon anularCupon(TokenClaims caller, String cuponId);
}
