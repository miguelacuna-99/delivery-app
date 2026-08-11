package es.delivery.manager.fidelidad.domain.repository;

import es.delivery.manager.fidelidad.domain.model.Cupon;

import java.util.List;
import java.util.Optional;

public interface CuponRepository {
    Cupon save(Cupon cupon);
    Optional<Cupon> findById(String id);
    Optional<Cupon> findByCodigo(String codigo);
    List<Cupon> findByComercioId(String comercioId);
}
