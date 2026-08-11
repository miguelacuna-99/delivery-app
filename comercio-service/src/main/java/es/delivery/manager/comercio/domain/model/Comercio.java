package es.delivery.manager.comercio.domain.model;

import es.delivery.manager.contracts.model.EstadoSuscripcion;
import es.delivery.manager.contracts.model.PlanSuscripcion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comercio {
    private String id;
    private String nombre;
    private String cif;
    private String direccion;
    private String telefono;
    private String email;
    private boolean activo;

    private PlanSuscripcion plan;
    private EstadoSuscripcion estadoSuscripcion;
    private Instant fechaInicioSuscripcion;
    private Instant fechaFinSuscripcion;

    /**
     * Un comercio opera (recibe pedidos, gestiona catalogo) mientras su
     * suscripcion no este SUSPENDIDA: ACTIVA y EN_GRACIA siguen operando.
     */
    public boolean esOperativo() {
        return activo && estadoSuscripcion != EstadoSuscripcion.SUSPENDIDA;
    }
}
