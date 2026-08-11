package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.model.TokenClaims;

/**
 * El repartidor introduce el numeroPedido que le da el cliente para marcar
 * la entrega (PAGADO -> ENTREGADO). Publica pedido.entregado.
 */
public interface EntregarPedidoUseCase {
    Pedido entregar(TokenClaims caller, String numeroPedido);
}
