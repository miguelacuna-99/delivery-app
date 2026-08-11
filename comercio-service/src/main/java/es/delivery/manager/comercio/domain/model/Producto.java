package es.delivery.manager.comercio.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Producto del catalogo de un comercio. Solo el ROOT o ADMIN del comercio
 * puede crear productos, cambiar precios, editarlos o eliminarlos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    private String id;
    private String comercioId;
    private String nombre;
    private String descripcion;
    private String ingredientes;
    private String imagenUrl;
    private BigDecimal precio;
    private boolean disponible;
}
