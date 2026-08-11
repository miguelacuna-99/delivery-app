package es.delivery.manager.comercio.application.usecase;

import es.delivery.manager.comercio.domain.model.Producto;

import java.util.List;

/**
 * Catalogo publico de un comercio (clientes navegan sin login).
 */
public interface ListProductosUseCase {
    List<Producto> listByComercio(String comercioId);
}
