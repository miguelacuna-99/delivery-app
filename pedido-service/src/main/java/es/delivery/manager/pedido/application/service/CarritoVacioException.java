package es.delivery.manager.pedido.application.service;

public class CarritoVacioException extends RuntimeException {
    public CarritoVacioException() {
        super("El carrito esta vacio");
    }
}
