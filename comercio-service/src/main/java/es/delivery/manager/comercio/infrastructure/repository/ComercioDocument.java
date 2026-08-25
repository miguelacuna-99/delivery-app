package es.delivery.manager.comercio.infrastructure.repository;

import es.delivery.manager.contracts.model.EstadoSuscripcion;
import es.delivery.manager.contracts.model.PlanSuscripcion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comercios")
public class ComercioDocument {
    @Id
    private String id;
    private String nombre;
    @Indexed(unique = true)
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
