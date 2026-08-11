package es.delivery.manager.fidelidad.infrastructure.repository;

import es.delivery.manager.fidelidad.domain.model.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoPuntosDocument {
    private String pedidoId;
    private TipoMovimiento tipo;
    private int puntos;
    private Instant fecha;
}
