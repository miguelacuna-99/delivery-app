package es.delivery.manager.comercio.infrastructure.controller.dto;

import es.delivery.manager.contracts.model.PlanSuscripcion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateComercioRequest {
    private String nombre;
    private String cif;
    private String direccion;
    private String telefono;
    private String email;
    private PlanSuscripcion plan;
}
