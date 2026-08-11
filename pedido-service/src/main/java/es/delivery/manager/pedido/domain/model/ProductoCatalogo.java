package es.delivery.manager.pedido.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Vista de solo lectura de un producto del catalogo de comercio-service.
 * Es la fuente de verdad del precio: lo que el cliente mande en el carrito
 * es solo lo que vio en pantalla, nunca lo que se cobra.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCatalogo {
    private String id;
    private String nombre;
    private String ingredientes;
    private String imagenUrl;
    private BigDecimal precio;
    private boolean disponible;
}
