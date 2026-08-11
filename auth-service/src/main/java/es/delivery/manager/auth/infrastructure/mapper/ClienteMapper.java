package es.delivery.manager.auth.infrastructure.mapper;

import es.delivery.manager.auth.domain.model.Cliente;
import es.delivery.manager.auth.infrastructure.controller.dto.ClienteResponse;
import es.delivery.manager.auth.infrastructure.controller.dto.RegisterClienteRequest;
import es.delivery.manager.auth.infrastructure.repository.ClienteDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    // password (raw) viaja temporalmente en passwordHash; el service lo encripta
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "tipo", ignore = true)
    Cliente toDomain(RegisterClienteRequest request);

    ClienteDocument toDocument(Cliente cliente);

    Cliente toDomain(ClienteDocument document);

    ClienteResponse toResponse(Cliente cliente);
}
