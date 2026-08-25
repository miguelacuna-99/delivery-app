package es.delivery.manager.comercio.infrastructure.controller.dto;

import es.delivery.manager.contracts.model.EstadoSuscripcion;
import es.delivery.manager.contracts.model.PlanSuscripcion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComercioResponse {
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
    private BigDecimal valorPuntoEuros;
}
