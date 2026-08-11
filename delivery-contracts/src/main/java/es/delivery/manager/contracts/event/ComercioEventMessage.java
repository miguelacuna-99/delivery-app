package es.delivery.manager.contracts.event;

import es.delivery.manager.contracts.model.EstadoSuscripcion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Evento de ciclo de vida de la suscripcion de un comercio
 * (routing keys comercio.suspendido / comercio.reactivado).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComercioEventMessage {
    private String routingKey;
    private String comercioId;
    private String nombre;
    private EstadoSuscripcion estadoSuscripcion;
    private Instant fechaFinSuscripcion;
    private Instant timestamp;
}
