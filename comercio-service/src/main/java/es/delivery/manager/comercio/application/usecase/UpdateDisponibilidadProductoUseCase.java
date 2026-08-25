package es.delivery.manager.comercio.application.usecase;

import es.delivery.manager.comercio.domain.model.Producto;
import es.delivery.manager.comercio.domain.model.TokenClaims;

/**
 * Solo ROOT o ADMIN del comercio activan/desactivan un producto, sin tocar el resto de sus datos.
 */
public interface UpdateDisponibilidadProductoUseCase {
    Producto updateDisponibilidad(TokenClaims caller, String productoId, boolean disponible);
}
