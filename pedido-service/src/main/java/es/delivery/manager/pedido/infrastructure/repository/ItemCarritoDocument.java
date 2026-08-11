package es.delivery.manager.pedido.infrastructure.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarritoDocument {
    private String productoId;
    private String nombre;
    private String ingredientes;
    private String imagenUrl;
    private BigDecimal precio;
    private int cantidad;
}
