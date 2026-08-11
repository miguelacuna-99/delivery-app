package es.delivery.manager.notificacion.application.service;

public class NotificacionNotFoundException extends RuntimeException {
    public NotificacionNotFoundException(String id) {
        super("Notificacion no encontrada: " + id);
    }
}
