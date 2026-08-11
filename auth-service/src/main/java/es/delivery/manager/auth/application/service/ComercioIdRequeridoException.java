package es.delivery.manager.auth.application.service;

public class ComercioIdRequeridoException extends RuntimeException {
    public ComercioIdRequeridoException() {
        super("El comercioId es obligatorio para registrar un cliente");
    }
}
