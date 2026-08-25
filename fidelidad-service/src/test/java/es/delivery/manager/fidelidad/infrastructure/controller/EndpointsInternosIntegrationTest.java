package es.delivery.manager.fidelidad.infrastructure.controller;

import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.fidelidad.application.usecase.*;
import es.delivery.manager.fidelidad.domain.model.CuentaPuntos;
import es.delivery.manager.fidelidad.domain.model.TokenClaims;
import es.delivery.manager.fidelidad.infrastructure.mapper.FidelidadMapperImpl;
import es.delivery.manager.fidelidad.infrastructure.security.AuthServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los endpoints internos que consume pedido-service exigen la clave de servicio
 * a servicio. Antes respondian a cualquiera que alcanzara el puerto, exponiendo
 * el saldo de puntos de cualquier cliente cuyo id se conociera.
 */
@WebMvcTest({PuntosController.class, CuponController.class})
@Import(FidelidadMapperImpl.class)
@TestPropertySource(properties = {
        "auth.service.url=http://localhost:8081",
        "service.api-key=clave-de-servicio-correcta"
})
class EndpointsInternosIntegrationTest {

    private static final String CLAVE_BUENA = "clave-de-servicio-correcta";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsultarSaldoUseCase consultarSaldoUseCase;
    @MockBean
    private GetCuentaPuntosUseCase getCuentaPuntosUseCase;
    @MockBean
    private CreateCuponUseCase createCuponUseCase;
    @MockBean
    private AnularCuponUseCase anularCuponUseCase;
    @MockBean
    private ListCuponesUseCase listCuponesUseCase;
    @MockBean
    private ValidarCuponUseCase validarCuponUseCase;
    @MockBean
    private AuthServiceClient authServiceClient;

    @Test
    void elSaldoSinClaveDeServicioResponde401() throws Exception {
        mockMvc.perform(get("/api/puntos/cliente-1/saldo").param("comercioId", "comercio-1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());

        verify(consultarSaldoUseCase, never()).saldoPuntos(anyString(), anyString());
    }

    @Test
    void elSaldoConClaveIncorrectaResponde401() throws Exception {
        mockMvc.perform(get("/api/puntos/cliente-1/saldo")
                        .param("comercioId", "comercio-1")
                        .header("X-Service-Key", "clave-inventada"))
                .andExpect(status().isUnauthorized());

        verify(consultarSaldoUseCase, never()).saldoPuntos(anyString(), anyString());
    }

    @Test
    void elSaldoConLaClaveCorrectaResponde200() throws Exception {
        when(consultarSaldoUseCase.saldoPuntos("cliente-1", "comercio-1")).thenReturn(250);

        mockMvc.perform(get("/api/puntos/cliente-1/saldo")
                        .param("comercioId", "comercio-1")
                        .header("X-Service-Key", CLAVE_BUENA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value("cliente-1"))
                .andExpect(jsonPath("$.saldo").value(250));
    }

    @Test
    void validarCuponSinClaveDeServicioResponde401() throws Exception {
        mockMvc.perform(get("/api/cupones/validar")
                        .param("codigo", "PROMO10")
                        .param("comercioId", "comercio-1")
                        .param("clienteId", "cliente-1"))
                .andExpect(status().isUnauthorized());

        verify(validarCuponUseCase, never()).validarCupon(anyString(), anyString(), anyString());
    }

    @Test
    void validarCuponConLaClaveCorrectaDevuelveElPorcentaje() throws Exception {
        when(validarCuponUseCase.validarCupon("PROMO10", "comercio-1", "cliente-1"))
                .thenReturn(Optional.of(new BigDecimal("10")));

        mockMvc.perform(get("/api/cupones/validar")
                        .header("X-Service-Key", CLAVE_BUENA)
                        .param("codigo", "PROMO10")
                        .param("comercioId", "comercio-1")
                        .param("clienteId", "cliente-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usable").value(true))
                .andExpect(jsonPath("$.porcentajeDescuento").value(10));
    }

    @Test
    void unCuponNoUsableDevuelveUsableFalse() throws Exception {
        when(validarCuponUseCase.validarCupon(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cupones/validar")
                        .header("X-Service-Key", CLAVE_BUENA)
                        .param("codigo", "CADUCADO")
                        .param("comercioId", "comercio-1")
                        .param("clienteId", "cliente-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usable").value(false));
    }

    @Test
    void losEndpointsDeUsuarioSiguenUsandoJwtNoLaClaveDeServicio() throws Exception {
        when(authServiceClient.validate(anyString())).thenReturn(Optional.of(TokenClaims.builder()
                .userId("cliente-1").tipo(TipoUsuario.CLIENTE).build()));
        when(getCuentaPuntosUseCase.getCuenta(any())).thenReturn(CuentaPuntos.builder()
                .clienteId("cliente-1").saldo(120).movimientos(List.of()).build());

        mockMvc.perform(get("/api/puntos/me").header("Authorization", "Bearer token-de-prueba"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(120));
    }
}
