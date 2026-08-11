package es.delivery.manager.fidelidad.infrastructure.controller;

import es.delivery.manager.fidelidad.application.usecase.ConsultarSaldoUseCase;
import es.delivery.manager.fidelidad.application.usecase.GetCuentaPuntosUseCase;
import es.delivery.manager.fidelidad.domain.model.TokenClaims;
import es.delivery.manager.fidelidad.infrastructure.controller.dto.CuentaPuntosResponse;
import es.delivery.manager.fidelidad.infrastructure.controller.dto.SaldoResponse;
import es.delivery.manager.fidelidad.infrastructure.mapper.FidelidadMapper;
import es.delivery.manager.fidelidad.infrastructure.security.RequestSecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/puntos")
@RequiredArgsConstructor
public class PuntosController {

    private final ConsultarSaldoUseCase consultarSaldoUseCase;
    private final GetCuentaPuntosUseCase getCuentaPuntosUseCase;
    private final FidelidadMapper fidelidadMapper;

    // El cliente consulta su cuenta con el historial de movimientos
    @GetMapping("/me")
    public CuentaPuntosResponse miCuenta() {
        TokenClaims caller = RequestSecurityContext.require();
        return fidelidadMapper.toResponse(getCuentaPuntosUseCase.getCuenta(caller));
    }

    // Saldo en el checkout (la llama pedido-service)
    @GetMapping("/{clienteId}/saldo")
    public SaldoResponse saldo(@PathVariable String clienteId) {
        return SaldoResponse.builder()
                .clienteId(clienteId)
                .saldo(consultarSaldoUseCase.saldoPuntos(clienteId))
                .build();
    }
}
