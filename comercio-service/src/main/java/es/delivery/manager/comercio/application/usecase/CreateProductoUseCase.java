package es.delivery.manager.comercio.application.usecase;

import es.delivery.manager.comercio.domain.model.Producto;
import es.delivery.manager.comercio.domain.model.TokenClaims;

/**
 * Solo ROOT o ADMIN del comercio crean productos del catalogo.
 */
public interface CreateProductoUseCase {
    Producto createProducto(TokenClaims caller, Producto producto);
}
