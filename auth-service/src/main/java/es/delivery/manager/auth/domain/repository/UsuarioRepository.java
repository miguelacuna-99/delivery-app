package es.delivery.manager.auth.domain.repository;

import es.delivery.manager.auth.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(String id);
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByMail(String mail);
    boolean existsByUsername(String username);
    List<Usuario> findByComercioId(String comercioId);
    void deleteById(String id);
}
