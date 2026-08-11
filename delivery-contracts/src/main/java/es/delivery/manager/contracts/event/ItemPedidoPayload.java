package es.delivery.manager.contracts.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoPayload {
    private String productoId;
    private String nombre;
    private BigDecimal precio;
    private int cantidad;
}
