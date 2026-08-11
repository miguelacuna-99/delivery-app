package es.delivery.manager.pedido.application.service;

import es.delivery.manager.contracts.model.EstadoPedido;

public class InvalidTransitionException extends RuntimeException {
    public InvalidTransitionException(EstadoPedido actual, EstadoPedido destino) {
        super("Transicion no permitida: " + actual + " -> " + destino);
    }
}
