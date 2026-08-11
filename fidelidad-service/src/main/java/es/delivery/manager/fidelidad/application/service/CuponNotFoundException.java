package es.delivery.manager.fidelidad.application.service;

public class CuponNotFoundException extends RuntimeException {
    public CuponNotFoundException(String id) {
        super("Cupon no encontrado: " + id);
    }
}
