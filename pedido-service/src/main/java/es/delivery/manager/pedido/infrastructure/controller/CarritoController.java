package es.delivery.manager.pedido.infrastructure.controller;

import es.delivery.manager.pedido.application.usecase.CheckoutUseCase;
import es.delivery.manager.pedido.application.usecase.ClearCarritoUseCase;
import es.delivery.manager.pedido.application.usecase.GetCarritoUseCase;
import es.delivery.manager.pedido.application.usecase.UpdateCarritoUseCase;
import es.delivery.manager.pedido.domain.model.Carrito;
import es.delivery.manager.pedido.domain.model.TokenClaims;
import es.delivery.manager.pedido.infrastructure.controller.dto.CarritoResponse;
import es.delivery.manager.pedido.infrastructure.controller.dto.PedidoResponse;
import es.delivery.manager.pedido.infrastructure.controller.dto.UpdateCarritoRequest;
import es.delivery.manager.pedido.infrastructure.mapper.PedidoMapper;
import es.delivery.manager.pedido.infrastructure.security.RequestSecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final GetCarritoUseCase getCarritoUseCase;
    private final UpdateCarritoUseCase updateCarritoUseCase;
    private final ClearCarritoUseCase clearCarritoUseCase;
    private final CheckoutUseCase checkoutUseCase;
    private final PedidoMapper pedidoMapper;

    @GetMapping
    public CarritoResponse getCarrito() {
        TokenClaims caller = RequestSecurityContext.require();
        return pedidoMapper.toCarritoResponse(getCarritoUseCase.getCarrito(caller));
    }

    @PutMapping
    public CarritoResponse updateCarrito(@RequestBody UpdateCarritoRequest request) {
        TokenClaims caller = RequestSecurityContext.require();
        Carrito carrito = updateCarritoUseCase.updateCarrito(caller, pedidoMapper.toDomain(request));
        return pedidoMapper.toCarritoResponse(carrito);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCarrito() {
        TokenClaims caller = RequestSecurityContext.require();
        clearCarritoUseCase.clearCarrito(caller);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<PedidoResponse> checkout() {
        TokenClaims caller = RequestSecurityContext.require();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoMapper.toResponse(checkoutUseCase.checkout(caller)));
    }
}
