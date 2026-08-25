package es.delivery.manager.pago.domain.repository;

import es.delivery.manager.pago.domain.model.Tarjeta;

import java.util.List;
import java.util.Optional;

public interface TarjetaRepository {
    Tarjeta save(Tarjeta tarjeta);
    Optional<Tarjeta> findById(String id);
    List<Tarjeta> findByClienteId(String clienteId);
    void deleteById(String id);
}
