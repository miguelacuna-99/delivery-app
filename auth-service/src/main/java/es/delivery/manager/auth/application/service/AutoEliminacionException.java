package es.delivery.manager.auth.application.service;

public class AutoEliminacionException extends RuntimeException {
    public AutoEliminacionException() {
        super("No puedes eliminarte a ti mismo");
    }
}
