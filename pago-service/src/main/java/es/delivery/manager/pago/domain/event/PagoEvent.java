package es.delivery.manager.pago.domain.event;

import es.delivery.manager.pago.domain.model.Pago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Evento de resultado de pago: routing keys pago.completado / pago.fallido.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoEvent {
    private String routingKey;
    private Pago pago;
    private Instant timestamp;
}
