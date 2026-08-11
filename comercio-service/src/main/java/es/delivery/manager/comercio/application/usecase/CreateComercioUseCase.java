package es.delivery.manager.comercio.application.usecase;

import es.delivery.manager.comercio.domain.model.Comercio;
import es.delivery.manager.contracts.model.PlanSuscripcion;

/**
 * Alta de un comercio por parte de la plataforma (X-Platform-Key).
 * Arranca con la suscripcion ACTIVA segun el plan contratado.
 */
public interface CreateComercioUseCase {
    Comercio createComercio(Comercio comercio, PlanSuscripcion plan);
}
