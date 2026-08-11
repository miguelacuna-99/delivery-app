package es.delivery.manager.pedido.application.service;

public class ComercioNoOperativoException extends RuntimeException {
    public ComercioNoOperativoException(String comercioId) {
        super("El comercio " + comercioId + " no admite pedidos en este momento");
    }
}
