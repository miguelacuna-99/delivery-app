package es.delivery.manager.auth.infrastructure.controller;

import es.delivery.manager.auth.application.service.ForbiddenOperationException;
import es.delivery.manager.auth.application.usecase.RegisterClienteUseCase;
import es.delivery.manager.auth.application.usecase.UpdateClienteContactoUseCase;
import es.delivery.manager.auth.application.usecase.ValidateTokenUseCase;
import es.delivery.manager.auth.domain.model.Cliente;
import es.delivery.manager.auth.domain.model.TokenClaims;
import es.delivery.manager.auth.infrastructure.controller.dto.ClienteResponse;
import es.delivery.manager.auth.infrastructure.controller.dto.RegisterClienteRequest;
import es.delivery.manager.auth.infrastructure.controller.dto.UpdateClienteContactoRequest;
import es.delivery.manager.auth.infrastructure.mapper.ClienteMapper;
import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final RegisterClienteUseCase registerClienteUseCase;
    private final UpdateClienteContactoUseCase updateClienteContactoUseCase;
    private final ValidateTokenUseCase validateTokenUseCase;
    private final ClienteMapper clienteMapper;

    // Registro publico: cualquiera puede darse de alta como cliente
    @PostMapping("/registro")
    public ResponseEntity<ClienteResponse> register(@RequestBody RegisterClienteRequest request) {
        Cliente cliente = registerClienteUseCase.register(clienteMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteMapper.toResponse(cliente));
    }

    // El cliente autenticado modifica sus propios datos de contacto
    @PutMapping("/me/contacto")
    public ResponseEntity<ClienteResponse> updateContacto(
            @RequestHeader("Authorization") String authorization,
            @RequestBody UpdateClienteContactoRequest request) {

        TokenClaims claims = validateBearer(authorization);
        if (claims.getTipo() != TipoUsuario.CLIENTE) {
            throw new ForbiddenOperationException("Solo un CLIENTE puede modificar sus datos de contacto");
        }

        Cliente cliente = updateClienteContactoUseCase.updateContacto(
                claims.getUserId(),
                request.getMail(),
                request.getDireccionDomicilio(),
                request.getTelefono());
        return ResponseEntity.ok(clienteMapper.toResponse(cliente));
    }

    private TokenClaims validateBearer(String authorization) {
        String token = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : authorization;
        return validateTokenUseCase.validate(token);
    }
}
