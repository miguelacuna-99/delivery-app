package es.delivery.manager.pago.infrastructure.controller;

import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.pago.application.service.TarjetaInvalidaException;
import es.delivery.manager.pago.application.service.TarjetaNotFoundException;
import es.delivery.manager.pago.application.usecase.EliminarTarjetaUseCase;
import es.delivery.manager.pago.application.usecase.GuardarTarjetaUseCase;
import es.delivery.manager.pago.application.usecase.ListTarjetasUseCase;
import es.delivery.manager.pago.domain.model.MarcaTarjeta;
import es.delivery.manager.pago.domain.model.Tarjeta;
import es.delivery.manager.pago.domain.model.TokenClaims;
import es.delivery.manager.pago.infrastructure.mapper.TarjetaMapperImpl;
import es.delivery.manager.pago.infrastructure.security.AuthServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarjetaController.class)
@Import(TarjetaMapperImpl.class)
@TestPropertySource(properties = {"auth.service.url=http://localhost:8081"})
class TarjetaControllerIntegrationTest {

    private static final String BEARER = "Bearer token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GuardarTarjetaUseCase guardarTarjetaUseCase;
    @MockBean
    private ListTarjetasUseCase listTarjetasUseCase;
    @MockBean
    private EliminarTarjetaUseCase eliminarTarjetaUseCase;
    @MockBean
    private AuthServiceClient authServiceClient;

    private void tokenValidoDeCliente() {
        when(authServiceClient.validate(anyString())).thenReturn(Optional.of(TokenClaims.builder()
                .userId("cliente-1").username("cliente").tipo(TipoUsuario.CLIENTE).build()));
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/tarjetas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void guardarUnaTarjetaDevuelve201() throws Exception {
        tokenValidoDeCliente();
        when(guardarTarjetaUseCase.guardarTarjeta(any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(Tarjeta.builder()
                        .id("t1").clienteId("cliente-1").titular("Ana Garcia")
                        .marca(MarcaTarjeta.VISA).ultimos4("4242")
                        .mesExpiracion(12).anioExpiracion(2030).fechaAlta(Instant.now())
                        .build());

        mockMvc.perform(post("/api/tarjetas")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numero": "4242424242424242",
                                  "titular": "Ana Garcia",
                                  "mesExpiracion": 12,
                                  "anioExpiracion": 2030
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ultimos4").value("4242"))
                .andExpect(jsonPath("$.marca").value("VISA"));
    }

    @Test
    void guardarUnaTarjetaInvalidaSeTraduceEn400() throws Exception {
        tokenValidoDeCliente();
        when(guardarTarjetaUseCase.guardarTarjeta(any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new TarjetaInvalidaException("Numero de tarjeta invalido"));

        mockMvc.perform(post("/api/tarjetas")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numero\": \"123\", \"titular\": \"Ana\", \"mesExpiracion\": 1, \"anioExpiracion\": 2099}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarDevuelveMisTarjetas() throws Exception {
        tokenValidoDeCliente();
        when(listTarjetasUseCase.listMisTarjetas(any())).thenReturn(List.of(Tarjeta.builder()
                .id("t1").clienteId("cliente-1").titular("Ana Garcia")
                .marca(MarcaTarjeta.VISA).ultimos4("4242")
                .mesExpiracion(12).anioExpiracion(2030).fechaAlta(Instant.now())
                .build()));

        mockMvc.perform(get("/api/tarjetas").header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ultimos4").value("4242"));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        tokenValidoDeCliente();

        mockMvc.perform(delete("/api/tarjetas/t1").header("Authorization", BEARER))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarUnaTarjetaAjenaSeTraduceEn404() throws Exception {
        tokenValidoDeCliente();
        org.mockito.Mockito.doThrow(new TarjetaNotFoundException("t2"))
                .when(eliminarTarjetaUseCase).eliminarTarjeta(any(), anyString());

        mockMvc.perform(delete("/api/tarjetas/t2").header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }
}
