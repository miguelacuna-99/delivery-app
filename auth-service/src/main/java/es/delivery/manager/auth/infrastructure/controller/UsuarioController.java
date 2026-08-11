package es.delivery.manager.auth.infrastructure.controller;

import es.delivery.manager.auth.application.service.ForbiddenOperationException;
import es.delivery.manager.auth.application.usecase.CreateUsuarioUseCase;
import es.delivery.manager.auth.application.usecase.ProvisionRootUseCase;
import es.delivery.manager.auth.application.usecase.ValidateTokenUseCase;
import es.delivery.manager.auth.domain.model.TokenClaims;
import es.delivery.manager.auth.domain.model.Usuario;
import es.delivery.manager.auth.infrastructure.controller.dto.CreateUsuarioRequest;
import es.delivery.manager.auth.infrastructure.controller.dto.ProvisionRootRequest;
import es.delivery.manager.auth.infrastructure.controller.dto.UsuarioResponse;
import es.delivery.manager.auth.infrastructure.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final ProvisionRootUseCase provisionRootUseCase;
    private final CreateUsuarioUseCase createUsuarioUseCase;
    private final ValidateTokenUseCase validateTokenUseCase;
    private final UsuarioMapper usuarioMapper;

    @Value("${platform.api-key}")
    private String platformApiKey;

    // Alta del ROOT de un comercio: solo la plataforma (API key), no expuesto a comercios
    @PostMapping("/root")
    public ResponseEntity<UsuarioResponse> provisionRoot(
            @RequestHeader("X-Platform-Key") String apiKey,
            @RequestBody ProvisionRootRequest request) {

        if (!platformApiKey.equals(apiKey)) {
            throw new ForbiddenOperationException("API key de plataforma invalida");
        }

        Usuario root = provisionRootUseCase.provisionRoot(usuarioMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioMapper.toResponse(root));
    }

    // ROOT o ADMIN del comercio crean usuarios ADMIN / PERSONAL / REPARTIDOR
    @PostMapping
    public ResponseEntity<UsuarioResponse> createUsuario(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CreateUsuarioRequest request) {

        TokenClaims caller = validateBearer(authorization);
        Usuario usuario = createUsuarioUseCase.createUsuario(caller, usuarioMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioMapper.toResponse(usuario));
    }

    private TokenClaims validateBearer(String authorization) {
        String token = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : authorization;
        return validateTokenUseCase.validate(token);
    }
}
