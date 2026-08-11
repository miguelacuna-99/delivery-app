package es.delivery.manager.contracts.event;

import es.delivery.manager.contracts.model.EstadoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Mensaje de resultado de pago publicado por pago-service
 * (routing keys pago.completado / pago.fallido).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoEventMessage {
    private String routingKey;
    private String pagoId;
    private String pedidoId;
    private String comercioId;
    private String clienteId;
    private BigDecimal importe;
    private EstadoPago estado;
    private Instant timestamp;
}
