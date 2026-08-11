package es.delivery.manager.fidelidad.application.service;

import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.fidelidad.application.usecase.ConsultarSaldoUseCase;
import es.delivery.manager.fidelidad.application.usecase.GetCuentaPuntosUseCase;
import es.delivery.manager.fidelidad.domain.model.CuentaPuntos;
import es.delivery.manager.fidelidad.domain.model.TokenClaims;
import es.delivery.manager.fidelidad.domain.repository.CuentaPuntosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PuntosService implements ConsultarSaldoUseCase, GetCuentaPuntosUseCase {

    private final CuentaPuntosRepository cuentaPuntosRepository;

    @Override
    public int saldoPuntos(String clienteId) {
        return cuentaPuntosRepository.findByClienteId(clienteId)
                .map(CuentaPuntos::getSaldo)
                .orElse(0);
    }

    @Override
    public CuentaPuntos getCuenta(TokenClaims caller) {
        if (caller.getTipo() != TipoUsuario.CLIENTE) {
            throw new ForbiddenOperationException("Operacion solo para clientes");
        }
        return cuentaPuntosRepository.findByClienteId(caller.getUserId())
                .orElseGet(() -> CuentaPuntos.builder()
                        .clienteId(caller.getUserId())
                        .saldo(0)
                        .movimientos(new ArrayList<>())
                        .build());
    }
}
