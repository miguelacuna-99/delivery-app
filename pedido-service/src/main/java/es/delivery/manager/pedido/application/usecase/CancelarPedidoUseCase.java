package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.Pedido;

/**
 * Cancela un pedido ACEPTADO que no ha llegado a cobrarse — porque la pasarela
 * denego el pago o porque vencio el plazo. Publica pedido.cancelado para que
 * fidelidad devuelva los puntos y el uso del cupon.
 */
public interface CancelarPedidoUseCase {
    Pedido cancelarPorFaltaDePago(String pedidoId, String motivo);
}
