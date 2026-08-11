package es.delivery.manager.comercio.application.service;

public class ProductoNotFoundException extends RuntimeException {
    public ProductoNotFoundException(String id) {
        super("Producto no encontrado: " + id);
    }
}
