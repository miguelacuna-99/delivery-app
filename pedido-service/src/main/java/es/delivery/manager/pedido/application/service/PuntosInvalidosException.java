package es.delivery.manager.pedido.application.service;

public class PuntosInvalidosException extends RuntimeException {
    public PuntosInvalidosException(int puntos) {
        super("Cantidad de puntos invalida: " + puntos);
    }
}
