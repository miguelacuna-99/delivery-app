package es.delivery.manager.comercio.infrastructure.repository;

import es.delivery.manager.comercio.domain.model.Producto;
import es.delivery.manager.comercio.domain.repository.ProductoRepository;
import es.delivery.manager.comercio.infrastructure.mapper.ProductoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductoRepositoryAdapter implements ProductoRepository {

    private final ProductoMongoRepository mongoRepository;
    private final ProductoMapper productoMapper;

    @Override
    public Producto save(Producto producto) {
        ProductoDocument doc = productoMapper.toDocument(producto);
        return productoMapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<Producto> findById(String id) {
        return mongoRepository.findById(id).map(productoMapper::toDomain);
    }

    @Override
    public List<Producto> findByComercioId(String comercioId) {
        return mongoRepository.findByComercioId(comercioId).stream()
                .map(productoMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }
}
