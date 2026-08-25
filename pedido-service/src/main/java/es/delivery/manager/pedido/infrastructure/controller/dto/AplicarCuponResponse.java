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
public class AplicarCuponResponse {
    private String codigo;
    private BigDecimal porcentajeDescuento;
}
