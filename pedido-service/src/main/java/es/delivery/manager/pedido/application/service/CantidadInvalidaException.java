package es.delivery.manager.pedido.application.service;

public class CantidadInvalidaException extends RuntimeException {
    public CantidadInvalidaException(String productoId, int cantidad) {
        super("Cantidad invalida para " + productoId + ": " + cantidad);
    }
}
