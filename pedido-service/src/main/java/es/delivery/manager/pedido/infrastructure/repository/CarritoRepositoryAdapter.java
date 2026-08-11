package es.delivery.manager.pedido.infrastructure.repository;

import es.delivery.manager.pedido.domain.model.Carrito;
import es.delivery.manager.pedido.domain.repository.CarritoRepository;
import es.delivery.manager.pedido.infrastructure.mapper.PedidoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CarritoRepositoryAdapter implements CarritoRepository {

    private final CarritoMongoRepository mongoRepository;
    private final PedidoMapper pedidoMapper;

    @Override
    public Carrito save(Carrito carrito) {
        CarritoDocument doc = pedidoMapper.toDocument(carrito);
        return pedidoMapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<Carrito> findByClienteId(String clienteId) {
        return mongoRepository.findByClienteId(clienteId).map(pedidoMapper::toDomain);
    }

    @Override
    public void deleteByClienteId(String clienteId) {
        mongoRepository.deleteByClienteId(clienteId);
    }
}
