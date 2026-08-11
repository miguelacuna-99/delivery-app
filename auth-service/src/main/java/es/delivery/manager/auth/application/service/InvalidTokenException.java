package es.delivery.manager.auth.application.service;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Token invalido o caducado");
    }
}
