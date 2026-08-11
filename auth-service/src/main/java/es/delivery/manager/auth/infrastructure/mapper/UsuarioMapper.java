package es.delivery.manager.auth.infrastructure.mapper;

import es.delivery.manager.auth.domain.model.TokenClaims;
import es.delivery.manager.auth.domain.model.Usuario;
import es.delivery.manager.auth.infrastructure.controller.dto.CreateUsuarioRequest;
import es.delivery.manager.auth.infrastructure.controller.dto.ProvisionRootRequest;
import es.delivery.manager.auth.infrastructure.controller.dto.UsuarioResponse;
import es.delivery.manager.auth.infrastructure.controller.dto.ValidateResponse;
import es.delivery.manager.auth.infrastructure.repository.UsuarioDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    // password (raw) viaja temporalmente en passwordHash; el service lo encripta
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "mustChangePassword", ignore = true)
    Usuario toDomain(ProvisionRootRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comercioId", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "mustChangePassword", ignore = true)
    Usuario toDomain(CreateUsuarioRequest request);

    UsuarioDocument toDocument(Usuario usuario);

    Usuario toDomain(UsuarioDocument document);

    UsuarioResponse toResponse(Usuario usuario);

    @Mapping(target = "valid", constant = "true")
    ValidateResponse toValidateResponse(TokenClaims claims);
}
