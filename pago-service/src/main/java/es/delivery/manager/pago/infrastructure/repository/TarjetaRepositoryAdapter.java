package es.delivery.manager.pago.infrastructure.repository;

import es.delivery.manager.pago.domain.model.Tarjeta;
import es.delivery.manager.pago.domain.repository.TarjetaRepository;
import es.delivery.manager.pago.infrastructure.mapper.TarjetaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TarjetaRepositoryAdapter implements TarjetaRepository {

    private final TarjetaMongoRepository mongoRepository;
    private final TarjetaMapper tarjetaMapper;

    @Override
    public Tarjeta save(Tarjeta tarjeta) {
        TarjetaDocument doc = tarjetaMapper.toDocument(tarjeta);
        return tarjetaMapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<Tarjeta> findById(String id) {
        return mongoRepository.findById(id).map(tarjetaMapper::toDomain);
    }

    @Override
    public List<Tarjeta> findByClienteId(String clienteId) {
        return mongoRepository.findByClienteId(clienteId).stream()
                .map(tarjetaMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }
}
