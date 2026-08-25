package es.delivery.manager.pedido.application.service;

import es.delivery.manager.contracts.model.EstadoPedido;
import es.delivery.manager.contracts.model.TipoUsuario;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CheckoutServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoPort productoPort;

    @Mock
    private FidelidadPort fidelidadPort;

    @Mock
    private ComercioPort comercioPort;

    @Mock
    private EstadoComercioRepository estadoComercioRepository;

    @Mock
    private PedidoEventPublisher eventPublisher;

    @InjectMocks
    private CheckoutService checkoutService;

    @BeforeEach
    void catalogoPorDefecto() {
        when(estadoComercioRepository.findByComercioId(anyString())).thenReturn(Optional.empty());
        when(productoPort.catalogoDe("comercio-1")).thenReturn(List.of(
                ProductoCatalogo.builder().id("p1").nombre("Pizza").ingredientes("Tomate, mozzarella")
                        .imagenUrl("https://cdn.example.com/pizza.jpg").precio(new BigDecimal("10.00")).disponible(true).build(),
                ProductoCatalogo.builder().id("p2").nombre("Refresco").precio(new BigDecimal("2.00")).disponible(true).build()));
        when(pedidoRepository.findByNumeroPedido(anyString())).thenReturn(Optional.empty());
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        when(comercioPort.getValorPunto("comercio-1")).thenReturn(new BigDecimal("0.01"));
    }

    private TokenClaims cliente() {
        return TokenClaims.builder()
                .userId("cliente-1")
                .username("cliente")
                .tipo(TipoUsuario.CLIENTE)
                .build();
    }

    private Carrito carrito(String codigoCupon, int puntos) {
        return Carrito.builder()
                .id("carrito-1")
                .clienteId("cliente-1")
                .comercioId("comercio-1")
                .items(List.of(
                        ItemCarrito.builder().productoId("p1").nombre("Pizza").precio(new BigDecimal("10.00")).cantidad(2).build(),
                        ItemCarrito.builder().productoId("p2").nombre("Refresco").precio(new BigDecimal("2.00")).cantidad(1).build()))
                .codigoCupon(codigoCupon)
                .puntosAplicados(puntos)
                .build();
    }

    private void enCarrito(Carrito carrito) {
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
    }

    private Carrito carritoCon(ItemCarrito item) {
        Carrito carrito = carrito(null, 0);
        return Carrito.builder()
                .id(carrito.getId())
                .clienteId(carrito.getClienteId())
                .comercioId(carrito.getComercioId())
                .items(List.of(item))
                .build();
    }

    @Test
    void checkoutCreaPedidoPendienteYPublicaPedidoCreado() {
        enCarrito(carrito(null, 0));

        Pedido pedido = checkoutService.checkout(cliente());

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(pedido.getSubtotal()).isEqualByComparingTo("22.00");
        assertThat(pedido.getTotal()).isEqualByComparingTo("22.00");
        assertThat(pedido.getNumeroPedido()).hasSize(6);
        assertThat(pedido.getFechaCreacion()).isNotNull();

        verify(carritoRepository).deleteByClienteId("cliente-1");
        ArgumentCaptor<PedidoEvent> captor = ArgumentCaptor.forClass(PedidoEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(EventType.PEDIDO_CREADO);
    }

    @Test
    void checkoutAplicaCupon() {
        enCarrito(carrito("PROMO10", 0));
        when(fidelidadPort.porcentajeCupon("PROMO10", "comercio-1", "cliente-1"))
                .thenReturn(Optional.of(new BigDecimal("10")));

        Pedido pedido = checkoutService.checkout(cliente());

        // subtotal 22.00, cupon 10% = 2.20
        assertThat(pedido.getDescuentoCupon()).isEqualByComparingTo("2.20");
        assertThat(pedido.getDescuentoPuntos()).isEqualByComparingTo("0");
        assertThat(pedido.getTotal()).isEqualByComparingTo("19.80");
    }

    @Test
    void checkoutAplicaPuntos() {
        enCarrito(carrito(null, 100));
        when(fidelidadPort.saldoPuntos("cliente-1", "comercio-1")).thenReturn(500);

        Pedido pedido = checkoutService.checkout(cliente());

        // subtotal 22.00, 100 puntos * 0.01 = 1.00
        assertThat(pedido.getDescuentoCupon()).isEqualByComparingTo("0");
        assertThat(pedido.getDescuentoPuntos()).isEqualByComparingTo("1.00");
        assertThat(pedido.getTotal()).isEqualByComparingTo("21.00");
    }

    @Test
    void checkoutRechazaCuponYPuntosJuntos() {
        // No deberia poder llegar a persistirse un carrito asi (aplicarCupon/
        // aplicarPuntos ya se rechazan mutuamente), pero el checkout tiene su
        // propia comprobacion de ultima linea de defensa
        enCarrito(carrito("PROMO10", 100));

        assertThatThrownBy(() -> checkoutService.checkout(cliente()))
                .isInstanceOf(CuponYPuntosExcluyentesException.class);

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void checkoutUsaElValorDePuntoDelComercio() {
        enCarrito(carrito(null, 100));
        when(fidelidadPort.saldoPuntos("cliente-1", "comercio-1")).thenReturn(500);
        when(comercioPort.getValorPunto("comercio-1")).thenReturn(new BigDecimal("0.02"));

        Pedido pedido = checkoutService.checkout(cliente());

        // 100 puntos * 0.02 = 2.00
        assertThat(pedido.getDescuentoPuntos()).isEqualByComparingTo("2.00");
    }

    @Test
    void checkoutRechazaCuponNoUsable() {
        enCarrito(carrito("CADUCADO", 0));
        when(fidelidadPort.porcentajeCupon("CADUCADO", "comercio-1", "cliente-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkoutService.checkout(cliente()))
                .isInstanceOf(CuponNoUsableException.class);

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void checkoutRechazaPuntosPorEncimaDelSaldo() {
        enCarrito(carrito(null, 1000));
        when(fidelidadPort.saldoPuntos("cliente-1", "comercio-1")).thenReturn(50);

        assertThatThrownBy(() -> checkoutService.checkout(cliente()))
                .isInstanceOf(PuntosInsuficientesException.class);

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void checkoutConCarritoVacioFalla() {
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkoutService.checkout(cliente()))
                .isInstanceOf(CarritoVacioException.class);
    }

    @Test
    void checkoutSoloParaClientes() {
        TokenClaims repartidor = TokenClaims.builder().userId("u1").tipo(TipoUsuario.REPARTIDOR).build();

        assertThatThrownBy(() -> checkoutService.checkout(repartidor))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    // --- Revalidacion contra el catalogo ---

    @Test
    void elPrecioYElNombreSalenDelCatalogoNoDelCarrito() {
        // El cliente manda un nombre falseado pero el precio correcto
        enCarrito(carritoCon(ItemCarrito.builder()
                .productoId("p1").nombre("Pizza GRATIS").precio(new BigDecimal("10.00")).cantidad(1).build()));

        Pedido pedido = checkoutService.checkout(cliente());

        assertThat(pedido.getItems()).singleElement()
                .satisfies(i -> {
                    assertThat(i.getNombre()).isEqualTo("Pizza");
                    assertThat(i.getPrecio()).isEqualByComparingTo("10.00");
                    assertThat(i.getIngredientes()).isEqualTo("Tomate, mozzarella");
                    assertThat(i.getImagenUrl()).isEqualTo("https://cdn.example.com/pizza.jpg");
                });
        assertThat(pedido.getTotal()).isEqualByComparingTo("10.00");
    }

    @Test
    void unPrecioManipuladoNoSeCobra() {
        enCarrito(carritoCon(ItemCarrito.builder()
                .productoId("p1").nombre("Pizza").precio(new BigDecimal("0.01")).cantidad(1).build()));

        assertThatThrownBy(() -> checkoutService.checkout(cliente()))
                .isInstanceOf(PrecioDesactualizadoException.class);

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void unProductoDeOtroComercioNoSePuedePedir() {
        enCarrito(carritoCon(ItemCarrito.builder()
                .productoId("ajeno").nombre("Algo").precio(new BigDecimal("5.00")).cantidad(1).build()));

        assertThatThrownBy(() -> checkoutService.checkout(cliente()))
                .isInstanceOf(ProductoNoDisponibleException.class);
    }

    @Test
    void unProductoNoDisponibleNoSePuedePedir() {
        when(productoPort.catalogoDe("comercio-1")).thenReturn(List.of(
                ProductoCatalogo.builder().id("p1").nombre("Pizza").precio(new BigDecimal("10.00")).disponible(false).build()));
        enCarrito(carritoCon(ItemCarrito.builder()
                .productoId("p1").nombre("Pizza").precio(new BigDecimal("10.00")).cantidad(1).build()));

        assertThatThrownBy(() -> checkoutService.checkout(cliente()))
                .isInstanceOf(ProductoNoDisponibleException.class);
    }

    @Test
    void unaCantidadNegativaNoRestaDelTotal() {
        enCarrito(carritoCon(ItemCarrito.builder()
                .productoId("p1").nombre("Pizza").precio(new BigDecimal("10.00")).cantidad(-5).build()));

        assertThatThrownBy(() -> checkoutService.checkout(cliente()))
                .isInstanceOf(CantidadInvalidaException.class);

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void unCarritoSinPrecioUsaElDelCatalogo() {
        enCarrito(carritoCon(ItemCarrito.builder()
                .productoId("p1").nombre(null).precio(null).cantidad(3).build()));

        Pedido pedido = checkoutService.checkout(cliente());

        assertThat(pedido.getTotal()).isEqualByComparingTo("30.00");
    }

    // --- Comercio suspendido ---

    @Test
    void unComercioSuspendidoNoAdmitePedidos() {
        when(estadoComercioRepository.findByComercioId("comercio-1")).thenReturn(Optional.of(
                EstadoComercio.builder().comercioId("comercio-1").operativo(false).build()));
        enCarrito(carrito(null, 0));

        assertThatThrownBy(() -> checkoutService.checkout(cliente()))
                .isInstanceOf(ComercioNoOperativoException.class);

        verify(pedidoRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void unComercioReactivadoVuelveAAdmitirPedidos() {
        when(estadoComercioRepository.findByComercioId("comercio-1")).thenReturn(Optional.of(
                EstadoComercio.builder().comercioId("comercio-1").operativo(true).build()));
        enCarrito(carrito(null, 0));

        Pedido pedido = checkoutService.checkout(cliente());

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
    }
}
