package es.delivery.manager.auth.application.service;

public class ClienteNotFoundException extends RuntimeException {
    public ClienteNotFoundException(String id) {
        super("Cliente no encontrado: " + id);
    }
}
