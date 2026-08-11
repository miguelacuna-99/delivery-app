package es.delivery.manager.fidelidad.application.service;

public class CodigoCuponDuplicadoException extends RuntimeException {
    public CodigoCuponDuplicadoException(String codigo) {
        super("Ya existe un cupon con codigo " + codigo);
    }
}
