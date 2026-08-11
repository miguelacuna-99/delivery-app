package es.delivery.manager.comercio.application.usecase;

import es.delivery.manager.comercio.domain.model.Comercio;

/**
 * La plataforma suspende la suscripcion de un comercio (impago).
 * Publica comercio.suspendido.
 */
public interface SuspenderSuscripcionUseCase {
    Comercio suspender(String comercioId);
}
