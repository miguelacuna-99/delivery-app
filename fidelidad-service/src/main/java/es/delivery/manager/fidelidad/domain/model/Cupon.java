package es.delivery.manager.fidelidad.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Cupon de descuento con X usos por usuario. Cuando un cliente agota sus usos,
 * el cupon queda inutilizable para el.
 * El ADMIN del comercio crea y anula cupones (estado ANULADO); si se supera
 * fechaCaducidad el cupon pasa a CADUCADO y no puede usarse.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cupon {
    private String id;
    private String comercioId;
    private String codigo;
    private BigDecimal porcentajeDescuento;
    private int usosMaximosPorUsuario;
    private List<UsoCupon> usos;
    private EstadoCupon estado;
    private Instant fechaCaducidad;

    public boolean esUsable(Instant ahora) {
        return estado == EstadoCupon.ACTIVO
                && (fechaCaducidad == null || ahora.isBefore(fechaCaducidad));
    }
}
