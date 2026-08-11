package es.delivery.manager.comercio.application.usecase;

import es.delivery.manager.comercio.domain.model.Comercio;
import es.delivery.manager.comercio.domain.model.TokenClaims;

/**
 * ROOT o ADMIN actualizan los datos de contacto de su propio comercio.
 */
public interface UpdateComercioUseCase {
    Comercio updateComercio(TokenClaims caller, Comercio cambios);
}
