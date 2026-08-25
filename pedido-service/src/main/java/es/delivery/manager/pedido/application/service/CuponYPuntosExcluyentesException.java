package es.delivery.manager.pedido.application.service;

public class CuponYPuntosExcluyentesException extends RuntimeException {
    public CuponYPuntosExcluyentesException() {
        super("Solo puedes usar un cupon o tus puntos, nunca ambos a la vez");
    }
}
