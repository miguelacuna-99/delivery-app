package es.delivery.manager.fidelidad.infrastructure.controller;

import es.delivery.manager.fidelidad.application.usecase.AnularCuponUseCase;
import es.delivery.manager.fidelidad.application.usecase.CreateCuponUseCase;
import es.delivery.manager.fidelidad.application.usecase.ListCuponesUseCase;
import es.delivery.manager.fidelidad.application.usecase.ValidarCuponUseCase;
import es.delivery.manager.fidelidad.domain.model.Cupon;
import es.delivery.manager.fidelidad.domain.model.TokenClaims;
import es.delivery.manager.fidelidad.infrastructure.controller.dto.CreateCuponRequest;
import es.delivery.manager.fidelidad.infrastructure.controller.dto.CuponResponse;
import es.delivery.manager.fidelidad.infrastructure.controller.dto.CuponValidacionResponse;
import es.delivery.manager.fidelidad.infrastructure.mapper.FidelidadMapper;
import es.delivery.manager.fidelidad.infrastructure.security.RequestSecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cupones")
@RequiredArgsConstructor
public class CuponController {

    private final CreateCuponUseCase createCuponUseCase;
    private final AnularCuponUseCase anularCuponUseCase;
    private final ListCuponesUseCase listCuponesUseCase;
    private final ValidarCuponUseCase validarCuponUseCase;
    private final FidelidadMapper fidelidadMapper;

    // Solo el ADMIN del comercio crea cupones
    @PostMapping
    public ResponseEntity<CuponResponse> createCupon(@RequestBody CreateCuponRequest request) {
        TokenClaims caller = RequestSecurityContext.require();
        Cupon cupon = createCuponUseCase.createCupon(caller, fidelidadMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(fidelidadMapper.toResponse(cupon));
    }

    @PostMapping("/{id}/anular")
    public CuponResponse anularCupon(@PathVariable String id) {
        TokenClaims caller = RequestSecurityContext.require();
        return fidelidadMapper.toResponse(anularCuponUseCase.anularCupon(caller, id));
    }

    @GetMapping
    public List<CuponResponse> listCupones() {
        TokenClaims caller = RequestSecurityContext.require();
        return listCuponesUseCase.listCupones(caller).stream()
                .map(fidelidadMapper::toResponse)
                .toList();
    }

    // Validacion en el checkout (la llama pedido-service)
    @GetMapping("/validar")
    public CuponValidacionResponse validar(@RequestParam String codigo,
                                           @RequestParam String comercioId,
                                           @RequestParam String clienteId) {
        Optional<BigDecimal> porcentaje = validarCuponUseCase.validarCupon(codigo, comercioId, clienteId);
        return CuponValidacionResponse.builder()
                .usable(porcentaje.isPresent())
                .porcentajeDescuento(porcentaje.orElse(null))
                .build();
    }
}
