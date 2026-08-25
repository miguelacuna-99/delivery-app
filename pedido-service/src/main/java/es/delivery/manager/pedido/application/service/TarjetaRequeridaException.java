package es.delivery.manager.pedido.application.service;

public class TarjetaRequeridaException extends RuntimeException {
    public TarjetaRequeridaException() {
        super("Hace falta elegir una tarjeta para pagar");
    }
}
