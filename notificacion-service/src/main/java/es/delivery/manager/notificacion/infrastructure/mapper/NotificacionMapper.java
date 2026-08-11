package es.delivery.manager.notificacion.infrastructure.mapper;

import es.delivery.manager.notificacion.domain.model.Notificacion;
import es.delivery.manager.notificacion.infrastructure.controller.dto.NotificacionResponse;
import es.delivery.manager.notificacion.infrastructure.repository.NotificacionDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificacionMapper {

    NotificacionDocument toDocument(Notificacion notificacion);

    Notificacion toDomain(NotificacionDocument document);

    NotificacionResponse toResponse(Notificacion notificacion);
}
