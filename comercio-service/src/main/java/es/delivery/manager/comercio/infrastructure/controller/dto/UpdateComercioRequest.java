package es.delivery.manager.comercio.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComercioRequest {
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    // Opcional: si no viene, se conserva el valor actual (a diferencia del
    // resto de campos de este DTO, que siempre se sobrescriben)
    private BigDecimal valorPuntoEuros;
}
