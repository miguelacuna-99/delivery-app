package es.delivery.manager.pago.infrastructure.repository;

import es.delivery.manager.pago.domain.model.Pago;
import es.delivery.manager.pago.domain.repository.PagoRepository;
import es.delivery.manager.pago.infrastructure.mapper.PagoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PagoRepositoryAdapter implements PagoRepository {

    private final PagoMongoRepository mongoRepository;
    private final PagoMapper pagoMapper;

    @Override
    public Pago save(Pago pago) {
        PagoDocument doc = pagoMapper.toDocument(pago);
        return pagoMapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<Pago> findById(String id) {
        return mongoRepository.findById(id).map(pagoMapper::toDomain);
    }

    @Override
    public Optional<Pago> findByPedidoId(String pedidoId) {
        return mongoRepository.findByPedidoId(pedidoId).map(pagoMapper::toDomain);
    }

    @Override
    public List<Pago> findByClienteId(String clienteId) {
        return mongoRepository.findByClienteId(clienteId).stream()
                .map(pagoMapper::toDomain)
                .toList();
    }
}
