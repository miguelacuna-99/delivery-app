package es.delivery.manager.auth.application.service;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("El username ya existe: " + username);
    }
}
