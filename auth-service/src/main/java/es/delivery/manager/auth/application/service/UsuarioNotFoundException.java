package es.delivery.manager.auth.application.service;

public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(String id) {
        super("Usuario no encontrado: " + id);
    }
}
