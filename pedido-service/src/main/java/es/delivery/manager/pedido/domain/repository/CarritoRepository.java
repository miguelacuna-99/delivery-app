package es.delivery.manager.pedido.domain.repository;

import es.delivery.manager.pedido.domain.model.Carrito;

import java.util.Optional;

public interface CarritoRepository {
    Carrito save(Carrito carrito);
    Optional<Carrito> findByClienteId(String clienteId);
    void deleteByClienteId(String clienteId);
}
