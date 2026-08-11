package es.delivery.manager.comercio.infrastructure.controller;

import es.delivery.manager.comercio.application.service.ForbiddenOperationException;
import es.delivery.manager.comercio.application.usecase.*;
import es.delivery.manager.comercio.domain.model.Comercio;
import es.delivery.manager.comercio.domain.model.TokenClaims;
import es.delivery.manager.comercio.infrastructure.controller.dto.ComercioResponse;
import es.delivery.manager.comercio.infrastructure.controller.dto.CreateComercioRequest;
import es.delivery.manager.comercio.infrastructure.controller.dto.RenovarSuscripcionRequest;
import es.delivery.manager.comercio.infrastructure.controller.dto.UpdateComercioRequest;
import es.delivery.manager.comercio.infrastructure.mapper.ComercioMapper;
import es.delivery.manager.comercio.infrastructure.security.RequestSecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comercios")
@RequiredArgsConstructor
public class ComercioController {

    private final CreateComercioUseCase createComercioUseCase;
    private final GetComercioUseCase getComercioUseCase;
    private final ListComerciosActivosUseCase listComerciosActivosUseCase;
    private final UpdateComercioUseCase updateComercioUseCase;
    private final SuspenderSuscripcionUseCase suspenderSuscripcionUseCase;
    private final RenovarSuscripcionUseCase renovarSuscripcionUseCase;
    private final ComercioMapper comercioMapper;

    @Value("${platform.api-key}")
    private String platformApiKey;

    // Alta de comercio: solo la plataforma (API key)
    @PostMapping
    public ResponseEntity<ComercioResponse> createComercio(
            @RequestHeader("X-Platform-Key") String apiKey,
            @RequestBody CreateComercioRequest request) {

        checkPlatformKey(apiKey);
        Comercio comercio = createComercioUseCase.createComercio(
                comercioMapper.toDomain(request), request.getPlan());
        return ResponseEntity.status(HttpStatus.CREATED).body(comercioMapper.toResponse(comercio));
    }

    // Listado publico de comercios operativos (app de clientes)
    @GetMapping
    public List<ComercioResponse> listActivos() {
        return listComerciosActivosUseCase.listActivos().stream()
                .map(comercioMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ComercioResponse getById(@PathVariable String id) {
        return comercioMapper.toResponse(getComercioUseCase.getById(id));
    }

    // Datos del comercio del usuario autenticado
    @GetMapping("/me")
    public ComercioResponse getMiComercio() {
        TokenClaims caller = RequestSecurityContext.require();
        return comercioMapper.toResponse(getComercioUseCase.getById(caller.getComercioId()));
    }

    // ROOT o ADMIN actualizan los datos de su comercio
    @PutMapping("/me")
    public ComercioResponse updateMiComercio(@RequestBody UpdateComercioRequest request) {
        TokenClaims caller = RequestSecurityContext.require();
        Comercio comercio = updateComercioUseCase.updateComercio(caller, comercioMapper.toDomain(request));
        return comercioMapper.toResponse(comercio);
    }

    // Suspension por impago: solo la plataforma
    @PostMapping("/{id}/suscripcion/suspender")
    public ComercioResponse suspender(
            @RequestHeader("X-Platform-Key") String apiKey,
            @PathVariable String id) {

        checkPlatformKey(apiKey);
        return comercioMapper.toResponse(suspenderSuscripcionUseCase.suspender(id));
    }

    // Renovacion (y reactivacion si estaba suspendida): solo la plataforma
    @PostMapping("/{id}/suscripcion/renovar")
    public ComercioResponse renovar(
            @RequestHeader("X-Platform-Key") String apiKey,
            @PathVariable String id,
            @RequestBody RenovarSuscripcionRequest request) {

        checkPlatformKey(apiKey);
        return comercioMapper.toResponse(renovarSuscripcionUseCase.renovar(id, request.getPlan()));
    }

    private void checkPlatformKey(String apiKey) {
        if (!platformApiKey.equals(apiKey)) {
            throw new ForbiddenOperationException("API key de plataforma invalida");
        }
    }
}
