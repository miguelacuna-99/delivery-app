package es.delivery.manager.pedido.application.service;

import es.delivery.manager.pedido.application.usecase.AplicarCuponUseCase;
import es.delivery.manager.pedido.application.usecase.AplicarPuntosUseCase;
import es.delivery.manager.pedido.application.usecase.ClearCarritoUseCase;
import es.delivery.manager.pedido.application.usecase.GetCarritoUseCase;
import es.delivery.manager.pedido.application.usecase.QuitarCuponUseCase;
import es.delivery.manager.pedido.application.usecase.QuitarPuntosUseCase;
import es.delivery.manager.pedido.application.usecase.UpdateCarritoUseCase;
import es.delivery.manager.pedido.domain.model.Carrito;
import es.delivery.manager.pedido.domain.model.TokenClaims;
import es.delivery.manager.pedido.domain.repository.CarritoRepository;
import es.delivery.manager.pedido.domain.service.ComercioPort;
import es.delivery.manager.pedido.domain.service.FidelidadPort;
import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarritoService implements GetCarritoUseCase, UpdateCarritoUseCase, ClearCarritoUseCase,
        AplicarCuponUseCase, QuitarCuponUseCase, AplicarPuntosUseCase, QuitarPuntosUseCase {

    private final CarritoRepository carritoRepository;
    private final FidelidadPort fidelidadPort;
    private final ComercioPort comercioPort;

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
        // El cupon y los puntos solo se fijan/quitan via aplicarCupon/quitarCupon
        // y aplicarPuntos/quitarPuntos (validados contra fidelidad-service): un
        // PUT de items nunca los cambia.
        carritoRepository.findByClienteId(caller.getUserId())
                .ifPresent(previo -> {
                    carrito.setId(previo.getId());
                    carrito.setCodigoCupon(previo.getCodigoCupon());
                    carrito.setPuntosAplicados(previo.getPuntosAplicados());
                });
        return carritoRepository.save(carrito);
    }

    @Override
    public void clearCarrito(TokenClaims caller) {
        checkCliente(caller);
        carritoRepository.deleteByClienteId(caller.getUserId());
    }

    @Override
    public BigDecimal aplicarCupon(TokenClaims caller, String codigo) {
        checkCliente(caller);
        Carrito carrito = carritoRepository.findByClienteId(caller.getUserId())
                .orElseThrow(CarritoVacioException::new);
        if (carrito.getPuntosAplicados() > 0) {
            throw new CuponYPuntosExcluyentesException();
        }
        BigDecimal porcentaje = fidelidadPort
                .porcentajeCupon(codigo, carrito.getComercioId(), caller.getUserId())
                .orElseThrow(() -> new CuponNoUsableException(codigo));
        carrito.setCodigoCupon(codigo);
        carritoRepository.save(carrito);
        return porcentaje;
    }

    @Override
    public void quitarCupon(TokenClaims caller) {
        checkCliente(caller);
        carritoRepository.findByClienteId(caller.getUserId()).ifPresent(carrito -> {
            carrito.setCodigoCupon(null);
            carritoRepository.save(carrito);
        });
    }

    @Override
    public BigDecimal aplicarPuntos(TokenClaims caller, int puntos) {
        checkCliente(caller);
        if (puntos <= 0) {
            throw new PuntosInvalidosException(puntos);
        }
        Carrito carrito = carritoRepository.findByClienteId(caller.getUserId())
                .orElseThrow(CarritoVacioException::new);
        if (carrito.getCodigoCupon() != null && !carrito.getCodigoCupon().isBlank()) {
            throw new CuponYPuntosExcluyentesException();
        }
        int saldo = fidelidadPort.saldoPuntos(caller.getUserId(), carrito.getComercioId());
        if (puntos > saldo) {
            throw new PuntosInsuficientesException(puntos, saldo);
        }
        carrito.setPuntosAplicados(puntos);
        carritoRepository.save(carrito);
        BigDecimal valorPunto = comercioPort.getValorPunto(carrito.getComercioId());
        return valorPunto.multiply(BigDecimal.valueOf(puntos));
    }

    @Override
    public void quitarPuntos(TokenClaims caller) {
        checkCliente(caller);
        carritoRepository.findByClienteId(caller.getUserId()).ifPresent(carrito -> {
            carrito.setPuntosAplicados(0);
            carritoRepository.save(carrito);
        });
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
