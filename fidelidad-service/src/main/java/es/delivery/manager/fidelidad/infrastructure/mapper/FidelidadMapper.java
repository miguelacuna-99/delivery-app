package es.delivery.manager.fidelidad.infrastructure.mapper;

import es.delivery.manager.fidelidad.domain.model.*;
import es.delivery.manager.fidelidad.infrastructure.controller.dto.CreateCuponRequest;
import es.delivery.manager.fidelidad.infrastructure.controller.dto.CuentaPuntosResponse;
import es.delivery.manager.fidelidad.infrastructure.controller.dto.CuponResponse;
import es.delivery.manager.fidelidad.infrastructure.repository.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FidelidadMapper {

    CuentaPuntosDocument toDocument(CuentaPuntos cuenta);

    CuentaPuntos toDomain(CuentaPuntosDocument document);

    MovimientoPuntosDocument toDocument(MovimientoPuntos movimiento);

    MovimientoPuntos toDomain(MovimientoPuntosDocument document);

    CuponDocument toDocument(Cupon cupon);

    Cupon toDomain(CuponDocument document);

    UsoCuponDocument toDocument(UsoCupon uso);

    UsoCupon toDomain(UsoCuponDocument document);

    // comercioId, estado y usos los fija el service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comercioId", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "usos", ignore = true)
    Cupon toDomain(CreateCuponRequest request);

    CuponResponse toResponse(Cupon cupon);

    CuentaPuntosResponse toResponse(CuentaPuntos cuenta);
}
