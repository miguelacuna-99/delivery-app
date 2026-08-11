package es.delivery.manager.auth.domain.repository;

import es.delivery.manager.auth.domain.model.Cliente;

import java.util.Optional;

public interface ClienteRepository {
    Cliente save(Cliente cliente);
    Optional<Cliente> findById(String id);
    Optional<Cliente> findByUsername(String username);
    Optional<Cliente> findByMail(String mail);
    boolean existsByUsername(String username);
}
