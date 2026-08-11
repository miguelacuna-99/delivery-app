package es.delivery.manager.fidelidad.infrastructure.security;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
