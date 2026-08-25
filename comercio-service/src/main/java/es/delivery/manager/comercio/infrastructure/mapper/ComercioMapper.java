package es.delivery.manager.comercio.infrastructure.mapper;

import es.delivery.manager.comercio.domain.model.Comercio;
import es.delivery.manager.comercio.infrastructure.controller.dto.ComercioResponse;
import es.delivery.manager.comercio.infrastructure.controller.dto.CreateComercioRequest;
import es.delivery.manager.comercio.infrastructure.controller.dto.UpdateComercioRequest;
import es.delivery.manager.comercio.infrastructure.repository.ComercioDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComercioMapper {

    // plan/suscripcion/valorPuntoEuros los fija el service a partir del plan de la request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "estadoSuscripcion", ignore = true)
    @Mapping(target = "fechaInicioSuscripcion", ignore = true)
    @Mapping(target = "fechaFinSuscripcion", ignore = true)
    @Mapping(target = "valorPuntoEuros", ignore = true)
    Comercio toDomain(CreateComercioRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cif", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "estadoSuscripcion", ignore = true)
    @Mapping(target = "fechaInicioSuscripcion", ignore = true)
    @Mapping(target = "fechaFinSuscripcion", ignore = true)
    Comercio toDomain(UpdateComercioRequest request);

    ComercioDocument toDocument(Comercio comercio);

    Comercio toDomain(ComercioDocument document);

    ComercioResponse toResponse(Comercio comercio);
}
