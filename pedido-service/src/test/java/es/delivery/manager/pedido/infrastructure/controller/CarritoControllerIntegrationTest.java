package es.delivery.manager.pedido.infrastructure.controller;

import es.delivery.manager.contracts.model.EstadoPedido;
import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.pedido.application.service.ComercioNoOperativoException;
import es.delivery.manager.pedido.application.service.ForbiddenOperationException;
import es.delivery.manager.pedido.application.service.PrecioDesactualizadoException;
import es.delivery.manager.pedido.application.usecase.CheckoutUseCase;
import es.delivery.manager.pedido.application.usecase.ClearCarritoUseCase;
import es.delivery.manager.pedido.application.usecase.GetCarritoUseCase;
import es.delivery.manager.pedido.application.usecase.UpdateCarritoUseCase;
import es.delivery.manager.pedido.domain.model.Carrito;
import es.delivery.manager.pedido.domain.model.ItemCarrito;
import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.model.TokenClaims;
import es.delivery.manager.pedido.infrastructure.mapper.PedidoMapperImpl;
import es.delivery.manager.pedido.infrastructure.security.AuthServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integracion del contexto web: rutas, serializacion JSON, el interceptor de
 * seguridad y el mapeo de excepciones a codigos HTTP. Los puertos van mockeados,
 * asi que no hace falta Mongo ni RabbitMQ.
 */
@WebMvcTest(CarritoController.class)
@Import(PedidoMapperImpl.class)
@TestPropertySource(properties = {
        "auth.service.url=http://localhost:8081",
        "comercio.service.url=http://localhost:8082",
        "fidelidad.service.url=http://localhost:8085",
        "service.api-key=test-service-key"
})
class CarritoControllerIntegrationTest {

    private static final String BEARER = "Bearer token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetCarritoUseCase getCarritoUseCase;
    @MockBean
    private UpdateCarritoUseCase updateCarritoUseCase;
    @MockBean
    private ClearCarritoUseCase clearCarritoUseCase;
    @MockBean
    private CheckoutUseCase checkoutUseCase;
    @MockBean
    private AuthServiceClient authServiceClient;

    private void tokenValidoDeCliente() {
        when(authServiceClient.validate(anyString())).thenReturn(Optional.of(TokenClaims.builder()
                .userId("cliente-1").username("cliente").tipo(TipoUsuario.CLIENTE).build()));
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void conTokenInvalidoResponde401() throws Exception {
        when(authServiceClient.validate(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/carrito").header("Authorization", BEARER))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devuelveElCarritoDelClienteAutenticado() throws Exception {
        tokenValidoDeCliente();
        when(getCarritoUseCase.getCarrito(any())).thenReturn(Carrito.builder()
                .id("carrito-1").clienteId("cliente-1").comercioId("comercio-1")
                .items(List.of(ItemCarrito.builder()
                        .productoId("p1").nombre("Pizza").precio(new BigDecimal("10.00")).cantidad(2).build()))
                .build());

        mockMvc.perform(get("/api/carrito").header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value("cliente-1"))
                .andExpect(jsonPath("$.items[0].productoId").value("p1"))
                .andExpect(jsonPath("$.items[0].precio").value(10.00));
    }

    @Test
    void actualizarElCarritoDeserializaLosItems() throws Exception {
        tokenValidoDeCliente();
        when(updateCarritoUseCase.updateCarrito(any(), any())).thenAnswer(inv -> inv.getArgument(1));

        mockMvc.perform(put("/api/carrito")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "comercioId": "comercio-1",
                                  "items": [
                                    {"productoId": "p1", "nombre": "Pizza", "precio": 10.00, "cantidad": 2}
                                  ],
                                  "codigoCupon": "PROMO10",
                                  "puntosAplicados": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comercioId").value("comercio-1"))
                .andExpect(jsonPath("$.codigoCupon").value("PROMO10"))
                .andExpect(jsonPath("$.puntosAplicados").value(100));
    }

    @Test
    void elCheckoutDevuelve201ConElNumeroDePedido() throws Exception {
        tokenValidoDeCliente();
        when(checkoutUseCase.checkout(any())).thenReturn(Pedido.builder()
                .id("pedido-1").numeroPedido("ABC234").comercioId("comercio-1").clienteId("cliente-1")
                .subtotal(new BigDecimal("22.00")).total(new BigDecimal("22.00"))
                .estado(EstadoPedido.PENDIENTE).fechaCreacion(Instant.now())
                .build());

        mockMvc.perform(post("/api/carrito/checkout").header("Authorization", BEARER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroPedido").value("ABC234"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void unPrecioDesactualizadoSeTraduceEn409() throws Exception {
        tokenValidoDeCliente();
        when(checkoutUseCase.checkout(any())).thenThrow(new PrecioDesactualizadoException(
                "p1", new BigDecimal("0.01"), new BigDecimal("10.00")));

        mockMvc.perform(post("/api/carrito/checkout").header("Authorization", BEARER))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("ha cambiado")));
    }

    @Test
    void unComercioSuspendidoSeTraduceEn409() throws Exception {
        tokenValidoDeCliente();
        when(checkoutUseCase.checkout(any())).thenThrow(new ComercioNoOperativoException("comercio-1"));

        mockMvc.perform(post("/api/carrito/checkout").header("Authorization", BEARER))
                .andExpect(status().isConflict());
    }

    @Test
    void unUsuarioQueNoEsClienteSeTraduceEn403() throws Exception {
        when(authServiceClient.validate(anyString())).thenReturn(Optional.of(TokenClaims.builder()
                .userId("u1").comercioId("comercio-1").tipo(TipoUsuario.REPARTIDOR).build()));
        when(checkoutUseCase.checkout(any()))
                .thenThrow(new ForbiddenOperationException("Operacion solo para clientes"));

        mockMvc.perform(post("/api/carrito/checkout").header("Authorization", BEARER))
                .andExpect(status().isForbidden());
    }
}
