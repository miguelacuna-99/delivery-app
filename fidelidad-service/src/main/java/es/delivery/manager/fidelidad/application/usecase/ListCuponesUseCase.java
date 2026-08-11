package es.delivery.manager.fidelidad.application.usecase;

import es.delivery.manager.fidelidad.domain.model.Cupon;
import es.delivery.manager.fidelidad.domain.model.TokenClaims;

import java.util.List;

/**
 * ROOT o ADMIN consultan los cupones de su comercio.
 */
public interface ListCuponesUseCase {
    List<Cupon> listCupones(TokenClaims caller);
}
