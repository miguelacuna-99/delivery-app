package es.delivery.manager.comercio.application.service;

public class ValorPuntoInvalidoException extends RuntimeException {
    public ValorPuntoInvalidoException() {
        super("El valor del punto debe ser mayor que 0");
    }
}
