package es.delivery.manager.auth.application.usecase;

import es.delivery.manager.auth.domain.model.TokenClaims;

/**
 * Solo ROOT elimina usuarios de su comercio.
 */
public interface DeleteUsuarioUseCase {
    void deleteUsuario(TokenClaims caller, String usuarioId);
}
