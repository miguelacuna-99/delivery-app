package es.delivery.manager.pedido.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Carrito de un cliente sobre un comercio concreto.
 * Al enviarse se convierte en Pedido (snapshot de items e importes).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Carrito {
    private String id;
    private String clienteId;
    private String comercioId;
    private List<ItemCarrito> items;
    private String codigoCupon;
    private int puntosAplicados;
}
