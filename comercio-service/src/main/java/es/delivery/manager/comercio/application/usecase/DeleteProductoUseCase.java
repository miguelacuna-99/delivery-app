package es.delivery.manager.comercio.application.usecase;

import es.delivery.manager.comercio.domain.model.TokenClaims;

/**
 * Solo ROOT o ADMIN del comercio eliminan productos del catalogo.
 */
public interface DeleteProductoUseCase {
    void deleteProducto(TokenClaims caller, String productoId);
}
