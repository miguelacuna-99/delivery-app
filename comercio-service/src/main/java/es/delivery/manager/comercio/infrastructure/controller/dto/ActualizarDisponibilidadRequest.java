package es.delivery.manager.comercio.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Activar/desactivar un producto sin tocar el resto de sus datos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarDisponibilidadRequest {
    private boolean disponible;
}
