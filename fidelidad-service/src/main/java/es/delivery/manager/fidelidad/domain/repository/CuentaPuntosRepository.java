package es.delivery.manager.fidelidad.domain.repository;

import es.delivery.manager.fidelidad.domain.model.CuentaPuntos;

import java.util.Optional;

public interface CuentaPuntosRepository {
    CuentaPuntos save(CuentaPuntos cuenta);
    Optional<CuentaPuntos> findByClienteId(String clienteId);
}
