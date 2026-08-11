package es.delivery.manager.pedido.application.service;

import es.delivery.manager.pedido.application.usecase.ClearCarritoUseCase;
import es.delivery.manager.pedido.application.usecase.GetCarritoUseCase;
import es.delivery.manager.pedido.application.usecase.UpdateCarritoUseCase;
import es.delivery.manager.pedido.domain.model.Carrito;
import es.delivery.manager.pedido.domain.model.TokenClaims;
import es.delivery.manager.pedido.domain.repository.CarritoRepository;
import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarritoService implements GetCarritoUseCase, UpdateCarritoUseCase, ClearCarritoUseCase {

    private final CarritoRepository carritoRepository;

    @Override
    public Carrito getCarrito(TokenClaims caller) {
        checkCliente(caller);
        return carritoRepository.findByClienteId(caller.getUserId())
                .orElseGet(() -> carritoVacio(caller.getUserId()));
    }

    @Override
    public Carrito updateCarrito(TokenClaims caller, Carrito carrito) {
        checkCliente(caller);
        // Multi-tenant: el carrito pertenece siempre al cliente del token; se
        // conserva el id del carrito previo para que el save sea un upsert
        carrito.setClienteId(caller.getUserId());
        // El comercioId sale del token del cliente, nunca del body: cada cliente
        // queda atado a un unico comercio desde su registro (webs vendidas por separado)
        carrito.setComercioId(caller.getComercioId());
        carritoRepository.findByClienteId(caller.getUserId())
                .ifPresent(previo -> carrito.setId(previo.getId()));
        return carritoRepository.save(carrito);
    }

    @Override
    public void clearCarrito(TokenClaims caller) {
        checkCliente(caller);
        carritoRepository.deleteByClienteId(caller.getUserId());
    }

    private Carrito carritoVacio(String clienteId) {
        return Carrito.builder()
                .clienteId(clienteId)
                .items(List.of())
                .build();
    }

    static void checkCliente(TokenClaims caller) {
        if (caller.getTipo() != TipoUsuario.CLIENTE) {
            throw new ForbiddenOperationException("Operacion solo para clientes");
        }
    }
}
