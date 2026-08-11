package es.delivery.manager.fidelidad.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaPuntosResponse {
    private String clienteId;
    private int saldo;
    private List<MovimientoPuntosDto> movimientos;
}
