package es.delivery.manager.fidelidad.application.usecase;

import es.delivery.manager.fidelidad.domain.model.CuentaPuntos;
import es.delivery.manager.fidelidad.domain.model.TokenClaims;

/**
 * El cliente consulta su cuenta de puntos con el historial de movimientos.
 */
public interface GetCuentaPuntosUseCase {
    CuentaPuntos getCuenta(TokenClaims caller);
}
