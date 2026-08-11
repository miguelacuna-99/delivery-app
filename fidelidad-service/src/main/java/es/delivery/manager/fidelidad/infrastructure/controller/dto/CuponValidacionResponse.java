package es.delivery.manager.fidelidad.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Respuesta de validacion de cupon consumida por pedido-service en el checkout.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuponValidacionResponse {
    private boolean usable;
    private BigDecimal porcentajeDescuento;
}
