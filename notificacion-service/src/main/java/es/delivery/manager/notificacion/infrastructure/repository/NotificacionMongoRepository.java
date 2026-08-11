package es.delivery.manager.notificacion.infrastructure.repository;

import es.delivery.manager.notificacion.domain.model.TipoNotificacion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificacionMongoRepository extends MongoRepository<NotificacionDocument, String> {
    List<NotificacionDocument> findByComercioIdAndTipoAndLeida(String comercioId, TipoNotificacion tipo, boolean leida);
}
