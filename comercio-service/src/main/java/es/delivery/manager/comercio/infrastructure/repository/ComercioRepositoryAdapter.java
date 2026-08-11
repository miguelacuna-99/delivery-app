package es.delivery.manager.comercio.infrastructure.repository;

import es.delivery.manager.comercio.domain.model.Comercio;
import es.delivery.manager.comercio.domain.repository.ComercioRepository;
import es.delivery.manager.comercio.infrastructure.mapper.ComercioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ComercioRepositoryAdapter implements ComercioRepository {

    private final ComercioMongoRepository mongoRepository;
    private final ComercioMapper comercioMapper;

    @Override
    public Comercio save(Comercio comercio) {
        ComercioDocument doc = comercioMapper.toDocument(comercio);
        return comercioMapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<Comercio> findById(String id) {
        return mongoRepository.findById(id).map(comercioMapper::toDomain);
    }

    @Override
    public List<Comercio> findAllActivos() {
        return mongoRepository.findByActivoTrue().stream()
                .map(comercioMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCif(String cif) {
        return mongoRepository.existsByCif(cif);
    }
}
