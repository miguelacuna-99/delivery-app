package es.delivery.manager.pedido.application.usecase;

import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.model.TokenClaims;

import java.util.List;

public interface ListPedidosClienteUseCase {
    List<Pedido> listPedidosCliente(TokenClaims caller);
}
