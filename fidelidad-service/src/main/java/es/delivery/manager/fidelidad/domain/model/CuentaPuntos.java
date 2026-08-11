package es.delivery.manager.fidelidad.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Cuenta de puntos de un cliente. Los puntos se ganan al pagar un pedido
 * (proporcional al gasto), se canjean como % de descuento y se devuelven
 * si el pago falla o expira (pedido CANCELADO).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaPuntos {
    private String id;
    private String clienteId;
    private int saldo;
    private List<MovimientoPuntos> movimientos;
}
