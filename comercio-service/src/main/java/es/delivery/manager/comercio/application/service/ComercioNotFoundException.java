package es.delivery.manager.comercio.application.service;

public class ComercioNotFoundException extends RuntimeException {
    public ComercioNotFoundException(String id) {
        super("Comercio no encontrado: " + id);
    }
}
