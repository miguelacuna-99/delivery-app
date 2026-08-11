package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.contracts.model.EstadoPedido;
import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.model.TokenClaims;

import java.util.List;

/**
 * Usuarios del comercio consultan los pedidos de su comercio por estado
 * (el comercioId sale siempre del token).
 */
public interface ListPedidosComercioUseCase {
    List<Pedido> listPedidosComercio(TokenClaims caller, EstadoPedido estado);
}
