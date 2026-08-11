package es.delivery.manager.notificacion.infrastructure.repository;

import es.delivery.manager.notificacion.domain.model.Notificacion;
import es.delivery.manager.notificacion.domain.model.TipoNotificacion;
import es.delivery.manager.notificacion.domain.repository.NotificacionRepository;
import es.delivery.manager.notificacion.infrastructure.mapper.NotificacionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificacionRepositoryAdapter implements NotificacionRepository {

    private final NotificacionMongoRepository mongoRepository;
    private final NotificacionMapper notificacionMapper;

    @Override
    public Notificacion save(Notificacion notificacion) {
        NotificacionDocument doc = notificacionMapper.toDocument(notificacion);
        return notificacionMapper.toDomain(mongoRepository.save(doc));
    }

    @Override
    public Optional<Notificacion> findById(String id) {
        return mongoRepository.findById(id).map(notificacionMapper::toDomain);
    }

    @Override
    public List<Notificacion> findByComercioIdAndTipoAndLeida(String comercioId, TipoNotificacion tipo, boolean leida) {
        return mongoRepository.findByComercioIdAndTipoAndLeida(comercioId, tipo, leida).stream()
                .map(notificacionMapper::toDomain)
                .toList();
    }
}
