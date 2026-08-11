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
public class ItemCarritoDto {
    private String productoId;
    private String nombre;
    private String ingredientes;
    private String imagenUrl;
    private BigDecimal precio;
    private int cantidad;
}
