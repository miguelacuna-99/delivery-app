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
public class ProductoResponse {
    private String id;
    private String comercioId;
    private String nombre;
    private String descripcion;
    private String ingredientes;
    private String imagenUrl;
    private BigDecimal precio;
    private boolean disponible;
}
