package es.delivery.manager.comercio.infrastructure.controller;

import es.delivery.manager.comercio.application.service.CifAlreadyExistsException;
import es.delivery.manager.comercio.application.usecase.*;
import es.delivery.manager.comercio.domain.model.Comercio;
import es.delivery.manager.comercio.infrastructure.mapper.ComercioMapperImpl;
import es.delivery.manager.comercio.infrastructure.security.AuthServiceClient;
import es.delivery.manager.contracts.model.EstadoSuscripcion;
import es.delivery.manager.contracts.model.PlanSuscripcion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Las operaciones de plataforma (alta y suscripcion) van con X-Platform-Key, no
 * con JWT: ni el ROOT ni el ADMIN del comercio pueden tocar su propia suscripcion.
 */
@WebMvcTest(ComercioController.class)
@Import(ComercioMapperImpl.class)
@TestPropertySource(properties = {
        "auth.service.url=http://localhost:8081",
        "platform.api-key=clave-de-plataforma"
})
class ComercioControllerIntegrationTest {

    private static final String CLAVE = "clave-de-plataforma";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateComercioUseCase createComercioUseCase;
    @MockBean
    private GetComercioUseCase getComercioUseCase;
    @MockBean
    private ListComerciosActivosUseCase listComerciosActivosUseCase;
    @MockBean
    private UpdateComercioUseCase updateComercioUseCase;
    @MockBean
    private SuspenderSuscripcionUseCase suspenderSuscripcionUseCase;
    @MockBean
    private RenovarSuscripcionUseCase renovarSuscripcionUseCase;
    @MockBean
    private AuthServiceClient authServiceClient;

    private Comercio comercio() {
        return Comercio.builder()
                .id("comercio-1").nombre("Pizzeria Demo").cif("B12345678")
                .activo(true).plan(PlanSuscripcion.MENSUAL)
                .estadoSuscripcion(EstadoSuscripcion.ACTIVA)
                .fechaInicioSuscripcion(Instant.now())
                .fechaFinSuscripcion(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();
    }

    private static final String ALTA = """
            {
              "nombre": "Pizzeria Demo",
              "cif": "B12345678",
              "direccion": "Calle Mayor 1",
              "telefono": "910000000",
              "email": "contacto@pizzeriademo.es",
              "plan": "MENSUAL"
            }
            """;

    @Test
    void elAltaConLaClaveDePlataformaDevuelve201() throws Exception {
        when(createComercioUseCase.createComercio(any(), any())).thenReturn(comercio());

        mockMvc.perform(post("/api/comercios")
                        .header("X-Platform-Key", CLAVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ALTA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("comercio-1"))
                .andExpect(jsonPath("$.estadoSuscripcion").value("ACTIVA"))
                .andExpect(jsonPath("$.plan").value("MENSUAL"));
    }

    @Test
    void elAltaConClaveIncorrectaResponde403() throws Exception {
        mockMvc.perform(post("/api/comercios")
                        .header("X-Platform-Key", "clave-inventada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ALTA))
                .andExpect(status().isForbidden());

        verify(createComercioUseCase, never()).createComercio(any(), any());
    }

    @Test
    void elAltaSinLaCabeceraResponde400() throws Exception {
        // Falta una cabecera obligatoria: Spring lo corta antes de llegar al controlador
        mockMvc.perform(post("/api/comercios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ALTA))
                .andExpect(status().isBadRequest());

        verify(createComercioUseCase, never()).createComercio(any(), any());
    }

    @Test
    void unCifDuplicadoSeTraduceEn409() throws Exception {
        when(createComercioUseCase.createComercio(any(), any()))
                .thenThrow(new CifAlreadyExistsException("B12345678"));

        mockMvc.perform(post("/api/comercios")
                        .header("X-Platform-Key", CLAVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ALTA))
                .andExpect(status().isConflict());
    }

    @Test
    void suspenderExigeLaClaveDePlataforma() throws Exception {
        mockMvc.perform(post("/api/comercios/comercio-1/suscripcion/suspender")
                        .header("X-Platform-Key", "clave-inventada"))
                .andExpect(status().isForbidden());

        verify(suspenderSuscripcionUseCase, never()).suspender(any());
    }

    @Test
    void renovarDevuelveLaSuscripcionActualizada() throws Exception {
        Comercio renovado = comercio();
        renovado.setPlan(PlanSuscripcion.ANUAL);
        when(renovarSuscripcionUseCase.renovar("comercio-1", PlanSuscripcion.ANUAL)).thenReturn(renovado);

        mockMvc.perform(post("/api/comercios/comercio-1/suscripcion/renovar")
                        .header("X-Platform-Key", CLAVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plan\": \"ANUAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("ANUAL"))
                .andExpect(jsonPath("$.estadoSuscripcion").value("ACTIVA"));
    }

    @Test
    void elListadoPublicoNoNecesitaToken() throws Exception {
        when(listComerciosActivosUseCase.listActivos()).thenReturn(List.of(comercio()));

        mockMvc.perform(get("/api/comercios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Pizzeria Demo"));
    }
}
