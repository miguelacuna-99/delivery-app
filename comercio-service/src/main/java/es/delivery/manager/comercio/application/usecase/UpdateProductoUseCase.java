package es.delivery.manager.comercio.application.usecase;

import es.delivery.manager.comercio.domain.model.Producto;
import es.delivery.manager.comercio.domain.model.TokenClaims;

/**
 * Solo ROOT o ADMIN del comercio editan productos (precio incluido).
 */
public interface UpdateProductoUseCase {
    Producto updateProducto(TokenClaims caller, String productoId, Producto cambios);
}
