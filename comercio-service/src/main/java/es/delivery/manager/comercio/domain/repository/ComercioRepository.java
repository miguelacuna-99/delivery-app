package es.delivery.manager.comercio.domain.repository;

import es.delivery.manager.comercio.domain.model.Comercio;

import java.util.List;
import java.util.Optional;

public interface ComercioRepository {
    Comercio save(Comercio comercio);
    Optional<Comercio> findById(String id);
    List<Comercio> findAllActivos();
    boolean existsByCif(String cif);
}
