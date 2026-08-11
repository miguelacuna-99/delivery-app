package es.delivery.manager.pedido.application.service;

public class ProductoNoDisponibleException extends RuntimeException {
    public ProductoNoDisponibleException(String productoId, String motivo) {
        super("Producto " + productoId + " no disponible: " + motivo);
    }
}
