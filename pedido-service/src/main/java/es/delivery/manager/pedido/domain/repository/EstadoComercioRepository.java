package es.delivery.manager.pedido.domain.repository;

import es.delivery.manager.pedido.domain.model.EstadoComercio;

import java.util.Optional;

public interface EstadoComercioRepository {
    EstadoComercio save(EstadoComercio estado);
    Optional<EstadoComercio> findByComercioId(String comercioId);
}
