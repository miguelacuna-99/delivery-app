package es.delivery.manager.pedido.infrastructure.controller;

import es.delivery.manager.contracts.model.EstadoPedido;
import es.delivery.manager.pedido.application.usecase.*;
import es.delivery.manager.pedido.domain.model.TokenClaims;
import es.delivery.manager.pedido.infrastructure.controller.dto.*;
import es.delivery.manager.pedido.infrastructure.mapper.PedidoMapper;
import es.delivery.manager.pedido.infrastructure.security.RequestSecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final ListPedidosClienteUseCase listPedidosClienteUseCase;
    private final ListPedidosComercioUseCase listPedidosComercioUseCase;
    private final AceptarPedidoUseCase aceptarPedidoUseCase;
    private final RechazarPedidoUseCase rechazarPedidoUseCase;
    private final EntregarPedidoUseCase entregarPedidoUseCase;
    private final AnularPedidoUseCase anularPedidoUseCase;
    private final PedidoMapper pedidoMapper;

    // Historial del cliente autenticado
    @GetMapping("/me")
    public List<PedidoResponse> misPedidos() {
        TokenClaims caller = RequestSecurityContext.require();
        return listPedidosClienteUseCase.listPedidosCliente(caller).stream()
                .map(pedidoMapper::toResponse)
                .toList();
    }

    // Pedidos del comercio del token filtrados por estado
    @GetMapping
    public List<PedidoResponse> pedidosComercio(@RequestParam EstadoPedido estado) {
        TokenClaims caller = RequestSecurityContext.require();
        return listPedidosComercioUseCase.listPedidosComercio(caller, estado).stream()
                .map(pedidoMapper::toResponse)
                .toList();
    }

    @PostMapping("/{id}/aceptar")
    public PedidoResponse aceptar(@PathVariable String id, @RequestBody AceptarPedidoRequest request) {
        TokenClaims caller = RequestSecurityContext.require();
        return pedidoMapper.toResponse(
                aceptarPedidoUseCase.aceptar(caller, id, request.getTiempoEstimadoMin()));
    }

    @PostMapping("/{id}/rechazar")
    public PedidoResponse rechazar(@PathVariable String id, @RequestBody RechazarPedidoRequest request) {
        TokenClaims caller = RequestSecurityContext.require();
        return pedidoMapper.toResponse(rechazarPedidoUseCase.rechazar(caller, id, request.getMensaje()));
    }

    // El repartidor introduce el numeroPedido que le da el cliente
    @PostMapping("/entregar")
    public PedidoResponse entregar(@RequestBody EntregarPedidoRequest request) {
        TokenClaims caller = RequestSecurityContext.require();
        return pedidoMapper.toResponse(entregarPedidoUseCase.entregar(caller, request.getNumeroPedido()));
    }

    @PostMapping("/{id}/anular")
    public PedidoResponse anular(@PathVariable String id, @RequestBody AnularPedidoRequest request) {
        TokenClaims caller = RequestSecurityContext.require();
        return pedidoMapper.toResponse(anularPedidoUseCase.anular(caller, id, request.getMotivo()));
    }
}
