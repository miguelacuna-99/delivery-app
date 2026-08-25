package es.delivery.manager.pago.infrastructure.mapper;

import es.delivery.manager.pago.domain.model.Tarjeta;
import es.delivery.manager.pago.infrastructure.controller.dto.TarjetaResponse;
import es.delivery.manager.pago.infrastructure.repository.TarjetaDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TarjetaMapper {
    TarjetaDocument toDocument(Tarjeta tarjeta);

    Tarjeta toDomain(TarjetaDocument document);

    TarjetaResponse toResponse(Tarjeta tarjeta);
}
