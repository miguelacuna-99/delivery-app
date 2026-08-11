package es.delivery.manager.pedido.domain.service;

import es.delivery.manager.pedido.domain.model.ProductoCatalogo;

import java.util.List;

/**
 * Catalogo de un comercio, consultado a comercio-service en el checkout
 * para revalidar precios y disponibilidad.
 */
public interface ProductoPort {
    List<ProductoCatalogo> catalogoDe(String comercioId);
}
