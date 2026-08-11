package es.delivery.manager.pedido.application.service;

public class CuponNoUsableException extends RuntimeException {
    public CuponNoUsableException(String codigo) {
        super("Cupon no usable: " + codigo);
    }
}
