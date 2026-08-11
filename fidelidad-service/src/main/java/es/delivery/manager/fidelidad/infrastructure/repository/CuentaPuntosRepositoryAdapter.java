package es.delivery.manager.fidelidad.infrastructure.repository;

import es.delivery.manager.fidelidad.domain.model.CuentaPuntos;
import es.delivery.manager.fidelidad.domain.repository.CuentaPuntosRepository;
import es.delivery.manager.fidelidad.infrastructure.mapper.FidelidadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CuentaPuntosRepositoryAdapter implements CuentaPuntosRepository {

    private final CuentaPuntosMongoRepository mongoRepository;
    private final FidelidadMapper fidelidadMapper;

    @Override
    public CuentaPuntos save(CuentaPuntos cuenta) {
        CuentaPuntosDocument doc = fidelidadMapper.toDocument(cuenta);
        return fidelidadMapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<CuentaPuntos> findByClienteId(String clienteId) {
        return mongoRepository.findByClienteId(clienteId).map(fidelidadMapper::toDomain);
    }
}
