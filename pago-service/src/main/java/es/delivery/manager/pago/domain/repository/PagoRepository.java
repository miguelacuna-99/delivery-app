package es.delivery.manager.pago.domain.repository;

import es.delivery.manager.pago.domain.model.Pago;

import java.util.List;
import java.util.Optional;

public interface PagoRepository {
    Pago save(Pago pago);
    Optional<Pago> findById(String id);
    Optional<Pago> findByPedidoId(String pedidoId);
    List<Pago> findByClienteId(String clienteId);
}
