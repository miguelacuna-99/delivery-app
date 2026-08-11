package es.delivery.manager.auth.application.usecase;

import es.delivery.manager.auth.domain.model.Usuario;

/**
 * Alta del usuario ROOT de un comercio, ejecutada por la plataforma
 * (protegida por API key, no por JWT).
 */
public interface ProvisionRootUseCase {
    Usuario provisionRoot(Usuario root);
}
