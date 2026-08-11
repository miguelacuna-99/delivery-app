package es.delivery.manager.auth.infrastructure.repository;

import es.delivery.manager.auth.domain.model.Cliente;
import es.delivery.manager.auth.domain.repository.ClienteRepository;
import es.delivery.manager.auth.infrastructure.mapper.ClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepository {

    private final ClienteMongoRepository mongoRepository;
    private final ClienteMapper clienteMapper;

    @Override
    public Cliente save(Cliente cliente) {
        ClienteDocument doc = clienteMapper.toDocument(cliente);
        return clienteMapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<Cliente> findById(String id) {
        return mongoRepository.findById(id).map(clienteMapper::toDomain);
    }

    @Override
    public Optional<Cliente> findByUsername(String username) {
        return mongoRepository.findByUsername(username).map(clienteMapper::toDomain);
    }

    @Override
    public Optional<Cliente> findByMail(String mail) {
        return mongoRepository.findByMail(mail).map(clienteMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return mongoRepository.existsByUsername(username);
    }
}
