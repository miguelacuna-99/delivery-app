package es.delivery.manager.pedido.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AplicarPuntosResponse {
    private int puntos;
    private BigDecimal descuentoEuros;
}
