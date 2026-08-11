package es.delivery.manager.comercio.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Alta y edicion de producto (el comercioId sale siempre del token).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {
    private String nombre;
    private String descripcion;
    private String ingredientes;
    private String imagenUrl;
    private BigDecimal precio;
    private boolean disponible;
}
