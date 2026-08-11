package es.delivery.manager.comercio.domain.repository;

import es.delivery.manager.comercio.domain.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    Producto save(Producto producto);
    Optional<Producto> findById(String id);
    List<Producto> findByComercioId(String comercioId);
    void deleteById(String id);
}
