package es.delivery.manager.auth.application.usecase;

import es.delivery.manager.auth.domain.model.TokenClaims;
import es.delivery.manager.auth.domain.model.Usuario;

/**
 * Creacion de usuarios de comercio (ADMIN / PERSONAL / REPARTIDOR)
 * por parte del ROOT o un ADMIN del mismo comercio.
 */
public interface CreateUsuarioUseCase {
    Usuario createUsuario(TokenClaims caller, Usuario usuario);
}
