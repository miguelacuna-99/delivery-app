package es.delivery.manager.auth.application.usecase;

import es.delivery.manager.auth.domain.model.TokenClaims;
import es.delivery.manager.auth.domain.model.Usuario;

import java.util.List;

/**
 * Solo ROOT ve la lista de usuarios de su comercio.
 */
public interface ListUsuariosUseCase {
    List<Usuario> listByComercio(TokenClaims caller);
}
