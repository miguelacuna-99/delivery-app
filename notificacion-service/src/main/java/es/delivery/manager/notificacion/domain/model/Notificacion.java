package es.delivery.manager.notificacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Notificacion para el comercio: bandejas PEDIDOS PENDIENTES y PEDIDOS PAGADOS.
 * Se crea al consumir eventos pedido.creado y pago.completado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {
    private String id;
    private String comercioId;
    private TipoNotificacion tipo;
    private String pedidoId;
    private String numeroPedido;
    private boolean leida;
    private Instant fecha;
}
