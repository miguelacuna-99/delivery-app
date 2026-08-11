package es.delivery.manager.pedido.application.service;

public class PedidoNotFoundException extends RuntimeException {
    public PedidoNotFoundException(String id) {
        super("Pedido no encontrado: " + id);
    }
}
