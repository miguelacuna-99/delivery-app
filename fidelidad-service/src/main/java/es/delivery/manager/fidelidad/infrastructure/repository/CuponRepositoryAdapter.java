package es.delivery.manager.fidelidad.infrastructure.repository;

import es.delivery.manager.fidelidad.domain.model.Cupon;
import es.delivery.manager.fidelidad.domain.repository.CuponRepository;
import es.delivery.manager.fidelidad.infrastructure.mapper.FidelidadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CuponRepositoryAdapter implements CuponRepository {

    private final CuponMongoRepository mongoRepository;
    private final FidelidadMapper fidelidadMapper;

    @Override
    public Cupon save(Cupon cupon) {
        CuponDocument doc = fidelidadMapper.toDocument(cupon);
        return fidelidadMapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<Cupon> findById(String id) {
        return mongoRepository.findById(id).map(fidelidadMapper::toDomain);
    }

    @Override
    public Optional<Cupon> findByCodigo(String codigo) {
        return mongoRepository.findByCodigo(codigo).map(fidelidadMapper::toDomain);
    }

    @Override
    public List<Cupon> findByComercioId(String comercioId) {
        return mongoRepository.findByComercioId(comercioId).stream()
                .map(fidelidadMapper::toDomain)
                .toList();
    }
}
