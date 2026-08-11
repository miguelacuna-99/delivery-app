package es.delivery.manager.comercio.infrastructure.mapper;

import es.delivery.manager.comercio.domain.model.Producto;
import es.delivery.manager.comercio.infrastructure.controller.dto.ProductoRequest;
import es.delivery.manager.comercio.infrastructure.controller.dto.ProductoResponse;
import es.delivery.manager.comercio.infrastructure.repository.ProductoDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    // comercioId lo fija el service a partir del token
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comercioId", ignore = true)
    Producto toDomain(ProductoRequest request);

    ProductoDocument toDocument(Producto producto);

    Producto toDomain(ProductoDocument document);

    ProductoResponse toResponse(Producto producto);
}
