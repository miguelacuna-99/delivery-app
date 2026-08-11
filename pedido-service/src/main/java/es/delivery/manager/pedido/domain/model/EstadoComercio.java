package es.delivery.manager.pedido.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Replica local de si un comercio admite pedidos, mantenida a partir de los
 * eventos comercio.suspendido / comercio.reactivado.
 *
 * Solo existe registro de los comercios cuyo estado ha cambiado alguna vez: un
 * comercio del que no sabemos nada se considera operativo (fail-open), porque
 * lo contrario dejaria el sistema sin poder pedir a nadie tras un arranque en frio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoComercio {
    private String id;
    private String comercioId;
    private boolean operativo;
    private Instant fechaActualizacion;
}
