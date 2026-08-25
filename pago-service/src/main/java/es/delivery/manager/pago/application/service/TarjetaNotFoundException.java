package es.delivery.manager.pago.application.service;

public class TarjetaNotFoundException extends RuntimeException {
    public TarjetaNotFoundException(String id) {
        super("Tarjeta no encontrada: " + id);
    }
}
