package es.delivery.manager.fidelidad.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCuponRequest {
    private String codigo;
    private BigDecimal porcentajeDescuento;
    private int usosMaximosPorUsuario;
    private Instant fechaCaducidad;
}
