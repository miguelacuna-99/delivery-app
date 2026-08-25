package es.delivery.manager.pedido.application.service;

import es.delivery.manager.pedido.application.usecase.CheckoutUseCase;
import es.delivery.manager.pedido.domain.event.EventType;
import es.delivery.manager.pedido.domain.event.PedidoEvent;
import es.delivery.manager.pedido.domain.event.PedidoEventPublisher;
import es.delivery.manager.pedido.domain.model.Carrito;
import es.delivery.manager.pedido.domain.model.EstadoComercio;
import es.delivery.manager.pedido.domain.model.ItemCarrito;
import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.model.ProductoCatalogo;
import es.delivery.manager.pedido.domain.model.TokenClaims;
import es.delivery.manager.pedido.domain.repository.CarritoRepository;
import es.delivery.manager.pedido.domain.repository.EstadoComercioRepository;
import es.delivery.manager.pedido.domain.repository.PedidoRepository;
import es.delivery.manager.pedido.domain.service.ComercioPort;
import es.delivery.manager.pedido.domain.service.FidelidadPort;
import es.delivery.manager.pedido.domain.service.ProductoPort;
import es.delivery.manager.contracts.model.EstadoPedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutService implements CheckoutUseCase {

    // Sin caracteres ambiguos (0/O, 1/I/L) para dictarlo al repartidor sin errores
    private static final String ALFABETO_NUMERO = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int LONGITUD_NUMERO = 6;

    private final CarritoRepository carritoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoPort productoPort;
    private final FidelidadPort fidelidadPort;
    private final ComercioPort comercioPort;
    private final EstadoComercioRepository estadoComercioRepository;
    private final PedidoEventPublisher eventPublisher;
    private final SecureRandom random = new SecureRandom();

    @Override
    public Pedido checkout(TokenClaims caller) {
        CarritoService.checkCliente(caller);
        Carrito carrito = carritoRepository.findByClienteId(caller.getUserId())
                .orElseThrow(CarritoVacioException::new);
        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            throw new CarritoVacioException();
        }
        checkComercioOperativo(carrito.getComercioId());
        // Ultima linea de defensa: aplicarCupon/aplicarPuntos ya se rechazan
        // mutuamente al fijarse en el carrito, esto nunca deberia disparar
        boolean tieneCupon = carrito.getCodigoCupon() != null && !carrito.getCodigoCupon().isBlank();
        if (tieneCupon && carrito.getPuntosAplicados() > 0) {
            throw new CuponYPuntosExcluyentesException();
        }

        // El carrito solo dice QUE se pide y CUANTO; el precio y el nombre
        // salen siempre del catalogo, nunca de lo que mande el cliente
        List<ItemCarrito> items = revalidarContraCatalogo(carrito);

        BigDecimal subtotal = items.stream()
                .map(i -> i.getPrecio().multiply(BigDecimal.valueOf(i.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal descuentoCupon = calcularDescuentoCupon(carrito, caller.getUserId(), subtotal);
        BigDecimal descuentoPuntos = calcularDescuentoPuntos(carrito, subtotal.subtract(descuentoCupon));
        BigDecimal total = subtotal.subtract(descuentoCupon).subtract(descuentoPuntos);

        Pedido pedido = Pedido.builder()
                .numeroPedido(generarNumeroPedido())
                .comercioId(carrito.getComercioId())
                .clienteId(caller.getUserId())
                .items(items)
                .subtotal(subtotal)
                .descuentoCupon(descuentoCupon)
                .descuentoPuntos(descuentoPuntos)
                .total(total)
                .estado(EstadoPedido.PENDIENTE)
                .codigoCupon(carrito.getCodigoCupon())
                .puntosAplicados(carrito.getPuntosAplicados())
                .fechaCreacion(Instant.now())
                .build();

        Pedido saved = pedidoRepository.save(pedido);
        carritoRepository.deleteByClienteId(caller.getUserId());
        eventPublisher.publish(PedidoEvent.builder()
                .type(EventType.PEDIDO_CREADO)
                .pedido(saved)
                .timestamp(Instant.now())
                .build());
        return saved;
    }

    /**
     * Reconstruye los items desde el catalogo del comercio. Rechaza el checkout si
     * un producto no existe, no esta disponible, viene con cantidad no positiva o
     * con un precio distinto del vigente.
     */
    private List<ItemCarrito> revalidarContraCatalogo(Carrito carrito) {
        Map<String, ProductoCatalogo> catalogo = productoPort.catalogoDe(carrito.getComercioId()).stream()
                .collect(Collectors.toMap(ProductoCatalogo::getId, Function.identity(), (a, b) -> a));

        List<ItemCarrito> items = new ArrayList<>(carrito.getItems().size());
        for (ItemCarrito item : carrito.getItems()) {
            ProductoCatalogo producto = catalogo.get(item.getProductoId());
            if (producto == null) {
                throw new ProductoNoDisponibleException(item.getProductoId(),
                        "no esta en el catalogo de este comercio");
            }
            if (!producto.isDisponible()) {
                throw new ProductoNoDisponibleException(item.getProductoId(), "marcado como no disponible");
            }
            // Sin esta comprobacion una cantidad negativa restaria del total
            if (item.getCantidad() <= 0) {
                throw new CantidadInvalidaException(item.getProductoId(), item.getCantidad());
            }
            if (item.getPrecio() != null && item.getPrecio().compareTo(producto.getPrecio()) != 0) {
                throw new PrecioDesactualizadoException(item.getProductoId(),
                        item.getPrecio(), producto.getPrecio());
            }
            items.add(ItemCarrito.builder()
                    .productoId(producto.getId())
                    .nombre(producto.getNombre())
                    .ingredientes(producto.getIngredientes())
                    .imagenUrl(producto.getImagenUrl())
                    .precio(producto.getPrecio())
                    .cantidad(item.getCantidad())
                    .build());
        }
        return items;
    }

    private void checkComercioOperativo(String comercioId) {
        boolean operativo = estadoComercioRepository.findByComercioId(comercioId)
                .map(EstadoComercio::isOperativo)
                .orElse(true);
        if (!operativo) {
            throw new ComercioNoOperativoException(comercioId);
        }
    }

    private BigDecimal calcularDescuentoCupon(Carrito carrito, String clienteId, BigDecimal subtotal) {
        if (carrito.getCodigoCupon() == null || carrito.getCodigoCupon().isBlank()) {
            return BigDecimal.ZERO;
        }
        BigDecimal porcentaje = fidelidadPort
                .porcentajeCupon(carrito.getCodigoCupon(), carrito.getComercioId(), clienteId)
                .orElseThrow(() -> new CuponNoUsableException(carrito.getCodigoCupon()));
        return subtotal.multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularDescuentoPuntos(Carrito carrito, BigDecimal restante) {
        int puntos = carrito.getPuntosAplicados();
        if (puntos <= 0) {
            return BigDecimal.ZERO;
        }
        int saldo = fidelidadPort.saldoPuntos(carrito.getClienteId(), carrito.getComercioId());
        if (puntos > saldo) {
            throw new PuntosInsuficientesException(puntos, saldo);
        }
        BigDecimal valorPunto = comercioPort.getValorPunto(carrito.getComercioId());
        // El descuento por puntos nunca deja el total en negativo
        return valorPunto.multiply(BigDecimal.valueOf(puntos)).min(restante);
    }

    private String generarNumeroPedido() {
        String numero;
        do {
            StringBuilder sb = new StringBuilder(LONGITUD_NUMERO);
            for (int i = 0; i < LONGITUD_NUMERO; i++) {
                sb.append(ALFABETO_NUMERO.charAt(random.nextInt(ALFABETO_NUMERO.length())));
            }
            numero = sb.toString();
        } while (pedidoRepository.findByNumeroPedido(numero).isPresent());
        return numero;
    }
}
