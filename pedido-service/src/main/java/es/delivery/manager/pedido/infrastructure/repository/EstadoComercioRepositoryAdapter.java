package es.delivery.manager.pedido.infrastructure.repository;

import es.delivery.manager.pedido.domain.model.EstadoComercio;
import es.delivery.manager.pedido.domain.repository.EstadoComercioRepository;
import es.delivery.manager.pedido.infrastructure.mapper.PedidoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EstadoComercioRepositoryAdapter implements EstadoComercioRepository {

    private final EstadoComercioMongoRepository mongoRepository;
    private final PedidoMapper pedidoMapper;

    @Override
    public EstadoComercio save(EstadoComercio estado) {
        // Upsert por comercioId: solo hay una fila por comercio
        mongoRepository.findByComercioId(estado.getComercioId())
                .ifPresent(previo -> estado.setId(previo.getId()));
        EstadoComercioDocument doc = pedidoMapper.toDocument(estado);
        return pedidoMapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<EstadoComercio> findByComercioId(String comercioId) {
        return mongoRepository.findByComercioId(comercioId).map(pedidoMapper::toDomain);
    }
}
