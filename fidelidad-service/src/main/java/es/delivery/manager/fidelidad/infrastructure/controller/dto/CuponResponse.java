package es.delivery.manager.fidelidad.infrastructure.controller.dto;

import es.delivery.manager.fidelidad.domain.model.EstadoCupon;
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
public class CuponResponse {
    private String id;
    private String comercioId;
    private String codigo;
    private BigDecimal porcentajeDescuento;
    private int usosMaximosPorUsuario;
    private EstadoCupon estado;
    private Instant fechaCaducidad;
}
