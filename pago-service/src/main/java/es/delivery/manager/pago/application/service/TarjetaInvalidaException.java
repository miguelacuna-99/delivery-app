package es.delivery.manager.pago.application.service;

public class TarjetaInvalidaException extends RuntimeException {
    public TarjetaInvalidaException(String motivo) {
        super(motivo);
    }
}
