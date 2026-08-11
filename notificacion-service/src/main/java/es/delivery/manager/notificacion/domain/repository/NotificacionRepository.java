package es.delivery.manager.notificacion.domain.repository;

import es.delivery.manager.notificacion.domain.model.Notificacion;
import es.delivery.manager.notificacion.domain.model.TipoNotificacion;

import java.util.List;
import java.util.Optional;

public interface NotificacionRepository {
    Notificacion save(Notificacion notificacion);
    Optional<Notificacion> findById(String id);
    List<Notificacion> findByComercioIdAndTipoAndLeida(String comercioId, TipoNotificacion tipo, boolean leida);
}
