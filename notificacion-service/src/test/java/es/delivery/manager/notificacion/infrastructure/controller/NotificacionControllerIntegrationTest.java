package es.delivery.manager.notificacion.infrastructure.controller;

import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.notificacion.application.service.ForbiddenOperationException;
import es.delivery.manager.notificacion.application.service.NotificacionNotFoundException;
import es.delivery.manager.notificacion.application.usecase.ListNotificacionesUseCase;
import es.delivery.manager.notificacion.application.usecase.MarcarLeidaUseCase;
import es.delivery.manager.notificacion.domain.model.Notificacion;
import es.delivery.manager.notificacion.domain.model.TipoNotificacion;
import es.delivery.manager.notificacion.domain.model.TokenClaims;
import es.delivery.manager.notificacion.infrastructure.mapper.NotificacionMapperImpl;
import es.delivery.manager.notificacion.infrastructure.security.AuthServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificacionController.class)
@Import(NotificacionMapperImpl.class)
@TestPropertySource(properties = "auth.service.url=http://localhost:8081")
class NotificacionControllerIntegrationTest {

    private static final String BEARER = "Bearer token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListNotificacionesUseCase listNotificacionesUseCase;
    @MockBean
    private MarcarLeidaUseCase marcarLeidaUseCase;
    @MockBean
    private AuthServiceClient authServiceClient;

    private void tokenDe(TipoUsuario tipo) {
        when(authServiceClient.validate(anyString())).thenReturn(Optional.of(TokenClaims.builder()
                .userId("user-1").comercioId("comercio-1").tipo(tipo).build()));
    }

    private Notificacion notificacion() {
        return Notificacion.builder()
                .id("notif-1").comercioId("comercio-1").tipo(TipoNotificacion.PEDIDO_PENDIENTE)
                .pedidoId("pedido-1").numeroPedido("ABC234").leida(false).fecha(Instant.now())
                .build();
    }

    @Test
    void laBandejaDevuelveLasNotificacionesSinLeer() throws Exception {
        tokenDe(TipoUsuario.PERSONAL);
        when(listNotificacionesUseCase.listNoLeidas(any(), any())).thenReturn(List.of(notificacion()));

        mockMvc.perform(get("/api/notificaciones")
                        .param("tipo", "PEDIDO_PENDIENTE")
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroPedido").value("ABC234"))
                .andExpect(jsonPath("$[0].leida").value(false));
    }

    @Test
    void unTipoDeBandejaInexistenteResponde400() throws Exception {
        tokenDe(TipoUsuario.PERSONAL);

        mockMvc.perform(get("/api/notificaciones")
                        .param("tipo", "NO_EXISTE")
                        .header("Authorization", BEARER))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unClienteNoPuedeVerLaBandeja() throws Exception {
        tokenDe(TipoUsuario.CLIENTE);
        when(listNotificacionesUseCase.listNoLeidas(any(), any()))
                .thenThrow(new ForbiddenOperationException("El tipo CLIENTE no puede consultar las notificaciones"));

        mockMvc.perform(get("/api/notificaciones")
                        .param("tipo", "PEDIDO_PENDIENTE")
                        .header("Authorization", BEARER))
                .andExpect(status().isForbidden());
    }

    @Test
    void marcarLeidaDevuelveLaNotificacionActualizada() throws Exception {
        tokenDe(TipoUsuario.PERSONAL);
        Notificacion leida = notificacion();
        leida.setLeida(true);
        when(marcarLeidaUseCase.marcarLeida(any(), any())).thenReturn(leida);

        mockMvc.perform(post("/api/notificaciones/notif-1/leida").header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leida").value(true));
    }

    @Test
    void unaNotificacionDeOtroComercioResponde404() throws Exception {
        tokenDe(TipoUsuario.ADMIN);
        when(marcarLeidaUseCase.marcarLeida(any(), any()))
                .thenThrow(new NotificacionNotFoundException("notif-ajena"));

        mockMvc.perform(post("/api/notificaciones/notif-ajena/leida").header("Authorization", BEARER))
                .andExpect(status().isNotFound());
    }
}
