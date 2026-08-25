package es.delivery.manager.auth.infrastructure.repository;

import es.delivery.manager.auth.domain.model.Usuario;
import es.delivery.manager.auth.domain.repository.UsuarioRepository;
import es.delivery.manager.auth.infrastructure.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioMongoRepository mongoRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioDocument doc = usuarioMapper.toDocument(usuario);
        return usuarioMapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<Usuario> findById(String id) {
        return mongoRepository.findById(id).map(usuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return mongoRepository.findByUsername(username).map(usuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByMail(String mail) {
        return mongoRepository.findByMail(mail).map(usuarioMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return mongoRepository.existsByUsername(username);
    }

    @Override
    public List<Usuario> findByComercioId(String comercioId) {
        return mongoRepository.findByComercioId(comercioId).stream()
                .map(usuarioMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }
}
