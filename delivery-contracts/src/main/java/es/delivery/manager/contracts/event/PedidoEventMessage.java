package es.delivery.manager.contracts.event;

import es.delivery.manager.contracts.model.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Mensaje de evento de pedido publicado en delivery.exchange (routing keys pedido.*
 * y pago.solicitado). Contrato compartido entre pedido-service y sus consumidores.
 *
 * Evolucion: anadir campos nuevos = version minor (compatible); eliminar o
 * renombrar campos = version major (los servicios antiguos siguen con la anterior).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEventMessage {
    private String routingKey;
    private String pedidoId;
    private String numeroPedido;
    private String comercioId;
    private String clienteId;
    private List<ItemPedidoPayload> items;
    private BigDecimal subtotal;
    private BigDecimal descuentoCupon;
    private BigDecimal descuentoPuntos;
    private BigDecimal total;
    private EstadoPedido estado;
    private Integer tiempoEstimadoMin;
    private String codigoCupon;
    private int puntosAplicados;
    private Instant timestamp;
}
