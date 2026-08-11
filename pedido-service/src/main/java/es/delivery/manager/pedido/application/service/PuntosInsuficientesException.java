package es.delivery.manager.pedido.application.service;

public class PuntosInsuficientesException extends RuntimeException {
    public PuntosInsuficientesException(int solicitados, int saldo) {
        super("Puntos insuficientes: solicitados " + solicitados + ", saldo " + saldo);
    }
}
