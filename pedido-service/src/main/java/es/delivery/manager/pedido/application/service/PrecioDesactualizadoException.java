package es.delivery.manager.pedido.application.service;

import java.math.BigDecimal;

/**
 * El precio que trae el carrito no es el del catalogo. Puede ser que el comercio
 * lo haya cambiado mientras el cliente compraba, o un intento de manipularlo:
 * en ambos casos se rechaza el checkout en vez de cobrar un importe que el
 * cliente no ha visto.
 */
public class PrecioDesactualizadoException extends RuntimeException {
    public PrecioDesactualizadoException(String productoId, BigDecimal enviado, BigDecimal actual) {
        super("El precio de " + productoId + " ha cambiado: enviado " + enviado
                + ", actual " + actual + ". Revisa el carrito.");
    }
}
