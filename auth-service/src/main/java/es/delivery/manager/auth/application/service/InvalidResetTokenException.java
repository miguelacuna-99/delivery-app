package es.delivery.manager.auth.application.service;

public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException() {
        super("Token de reseteo invalido, usado o caducado");
    }
}
