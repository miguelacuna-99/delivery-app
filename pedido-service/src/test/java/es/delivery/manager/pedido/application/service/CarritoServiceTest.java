package es.delivery.manager.pedido.application.service;

import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.pedido.domain.model.Carrito;
import es.delivery.manager.pedido.domain.model.TokenClaims;
import es.delivery.manager.pedido.domain.repository.CarritoRepository;
import es.delivery.manager.pedido.domain.service.ComercioPort;
import es.delivery.manager.pedido.domain.service.FidelidadPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private FidelidadPort fidelidadPort;

    @Mock
    private ComercioPort comercioPort;

    @InjectMocks
    private CarritoService carritoService;

    private TokenClaims cliente(String comercioId) {
        return TokenClaims.builder()
                .userId("cliente-1")
                .username("ana")
                .comercioId(comercioId)
                .tipo(TipoUsuario.CLIENTE)
                .build();
    }

    // El cliente esta atado a un unico comercio desde su registro: el comercioId
    // del carrito tiene que salir del token, nunca del que mande el cliente en el body
    @Test
    void updateCarritoIgnoraElComercioIdDelBodyYUsaElDelToken() {
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.empty());
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Carrito carritoConComercioAjeno = Carrito.builder()
                .comercioId("comercio-ajeno")
                .items(List.of())
                .build();

        Carrito actualizado = carritoService.updateCarrito(cliente("comercio-1"), carritoConComercioAjeno);

        assertThat(actualizado.getComercioId()).isEqualTo("comercio-1");
        assertThat(actualizado.getClienteId()).isEqualTo("cliente-1");
    }

    // El cupon solo se fija via aplicarCupon (validado); un PUT de items no
    // puede colar uno sin pasar por fidelidad-service
    @Test
    void updateCarritoIgnoraElCodigoCuponDelBodyYConservaElYaAplicado() {
        Carrito previo = Carrito.builder()
                .id("carrito-1").clienteId("cliente-1").comercioId("comercio-1")
                .items(List.of()).codigoCupon("PROMO10").build();
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(previo));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Carrito conCuponSinValidar = Carrito.builder()
                .comercioId("comercio-1").items(List.of()).codigoCupon("COLADO").build();

        Carrito actualizado = carritoService.updateCarrito(cliente("comercio-1"), conCuponSinValidar);

        assertThat(actualizado.getCodigoCupon()).isEqualTo("PROMO10");
    }

    // Mismo cierre de hueco que el cupon: los puntos solo se fijan via aplicarPuntos
    @Test
    void updateCarritoIgnoraLosPuntosDelBodyYConservaLosYaAplicados() {
        Carrito previo = Carrito.builder()
                .id("carrito-1").clienteId("cliente-1").comercioId("comercio-1")
                .items(List.of()).puntosAplicados(100).build();
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(previo));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Carrito conPuntosSinValidar = Carrito.builder()
                .comercioId("comercio-1").items(List.of()).puntosAplicados(9999).build();

        Carrito actualizado = carritoService.updateCarrito(cliente("comercio-1"), conPuntosSinValidar);

        assertThat(actualizado.getPuntosAplicados()).isEqualTo(100);
    }

    @Test
    void aplicarCuponValidoLoFijaYDevuelveElPorcentaje() {
        Carrito carrito = Carrito.builder()
                .id("carrito-1").clienteId("cliente-1").comercioId("comercio-1").items(List.of()).build();
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fidelidadPort.porcentajeCupon("PROMO10", "comercio-1", "cliente-1"))
                .thenReturn(Optional.of(new BigDecimal("10")));

        BigDecimal porcentaje = carritoService.aplicarCupon(cliente("comercio-1"), "PROMO10");

        assertThat(porcentaje).isEqualByComparingTo("10");
        assertThat(carrito.getCodigoCupon()).isEqualTo("PROMO10");
    }

    @Test
    void aplicarCuponNoUsableLanza400YNoLoFija() {
        Carrito carrito = Carrito.builder()
                .id("carrito-1").clienteId("cliente-1").comercioId("comercio-1").items(List.of()).build();
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
        when(fidelidadPort.porcentajeCupon("CADUCADO", "comercio-1", "cliente-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.aplicarCupon(cliente("comercio-1"), "CADUCADO"))
                .isInstanceOf(CuponNoUsableException.class);

        verify(carritoRepository, never()).save(any());
    }

    @Test
    void aplicarCuponConPuntosYaAplicadosLanzaExcluyentes() {
        Carrito carrito = Carrito.builder()
                .id("carrito-1").clienteId("cliente-1").comercioId("comercio-1")
                .items(List.of()).puntosAplicados(100).build();
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));

        assertThatThrownBy(() -> carritoService.aplicarCupon(cliente("comercio-1"), "PROMO10"))
                .isInstanceOf(CuponYPuntosExcluyentesException.class);

        verify(carritoRepository, never()).save(any());
    }

    @Test
    void aplicarCuponSinCarritoLanzaCarritoVacio() {
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.aplicarCupon(cliente("comercio-1"), "PROMO10"))
                .isInstanceOf(CarritoVacioException.class);
    }

    @Test
    void quitarCuponLoBorraDelCarrito() {
        Carrito carrito = Carrito.builder()
                .id("carrito-1").clienteId("cliente-1").comercioId("comercio-1")
                .items(List.of()).codigoCupon("PROMO10").build();
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        carritoService.quitarCupon(cliente("comercio-1"));

        assertThat(carrito.getCodigoCupon()).isNull();
    }

    @Test
    void aplicarPuntosValidoLoFijaYDevuelveElDescuento() {
        Carrito carrito = Carrito.builder()
                .id("carrito-1").clienteId("cliente-1").comercioId("comercio-1").items(List.of()).build();
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fidelidadPort.saldoPuntos("cliente-1", "comercio-1")).thenReturn(500);
        when(comercioPort.getValorPunto("comercio-1")).thenReturn(new BigDecimal("0.01"));

        BigDecimal descuento = carritoService.aplicarPuntos(cliente("comercio-1"), 100);

        assertThat(descuento).isEqualByComparingTo("1.00");
        assertThat(carrito.getPuntosAplicados()).isEqualTo(100);
    }

    @Test
    void aplicarPuntosPorEncimaDelSaldoLanzaInsuficientes() {
        Carrito carrito = Carrito.builder()
                .id("carrito-1").clienteId("cliente-1").comercioId("comercio-1").items(List.of()).build();
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
        when(fidelidadPort.saldoPuntos("cliente-1", "comercio-1")).thenReturn(50);

        assertThatThrownBy(() -> carritoService.aplicarPuntos(cliente("comercio-1"), 100))
                .isInstanceOf(PuntosInsuficientesException.class);

        verify(carritoRepository, never()).save(any());
    }

    @Test
    void aplicarPuntosNoPositivosLanzaInvalidos() {
        assertThatThrownBy(() -> carritoService.aplicarPuntos(cliente("comercio-1"), 0))
                .isInstanceOf(PuntosInvalidosException.class);

        verify(carritoRepository, never()).findByClienteId(any());
    }

    @Test
    void aplicarPuntosConCuponYaAplicadoLanzaExcluyentes() {
        Carrito carrito = Carrito.builder()
                .id("carrito-1").clienteId("cliente-1").comercioId("comercio-1")
                .items(List.of()).codigoCupon("PROMO10").build();
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));

        assertThatThrownBy(() -> carritoService.aplicarPuntos(cliente("comercio-1"), 100))
                .isInstanceOf(CuponYPuntosExcluyentesException.class);

        verify(carritoRepository, never()).save(any());
    }

    @Test
    void quitarPuntosLosBorraDelCarrito() {
        Carrito carrito = Carrito.builder()
                .id("carrito-1").clienteId("cliente-1").comercioId("comercio-1")
                .items(List.of()).puntosAplicados(100).build();
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        carritoService.quitarPuntos(cliente("comercio-1"));

        assertThat(carrito.getPuntosAplicados()).isZero();
    }
}
