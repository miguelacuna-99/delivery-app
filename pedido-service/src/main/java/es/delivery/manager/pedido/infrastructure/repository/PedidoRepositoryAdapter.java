package es.delivery.manager.pedido.infrastructure.repository;

import es.delivery.manager.contracts.model.EstadoPedido;
import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.repository.PedidoRepository;
import es.delivery.manager.pedido.infrastructure.mapper.PedidoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PedidoRepositoryAdapter implements PedidoRepository {

    private final PedidoMongoRepository mongoRepository;
    private final PedidoMapper pedidoMapper;

    @Override
    public Pedido save(Pedido pedido) {
        PedidoDocument doc = pedidoMapper.toDocument(pedido);
        return pedidoMapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<Pedido> findById(String id) {
        return mongoRepository.findById(id).map(pedidoMapper::toDomain);
    }

    @Override
    public Optional<Pedido> findByNumeroPedido(String numeroPedido) {
        return mongoRepository.findByNumeroPedido(numeroPedido).map(pedidoMapper::toDomain);
    }

    @Override
    public List<Pedido> findByClienteId(String clienteId) {
        return mongoRepository.findByClienteId(clienteId).stream()
                .map(pedidoMapper::toDomain)
                .toList();
    }

    @Override
    public List<Pedido> findByComercioIdAndEstado(String comercioId, EstadoPedido estado) {
        return mongoRepository.findByComercioIdAndEstado(comercioId, estado).stream()
                .map(pedidoMapper::toDomain)
                .toList();
    }

    @Override
    public List<Pedido> findByEstadoAndFechaAceptacionBefore(EstadoPedido estado, Instant limite) {
        return mongoRepository.findByEstadoAndFechaAceptacionBefore(estado, limite).stream()
                .map(pedidoMapper::toDomain)
                .toList();
    }
}
