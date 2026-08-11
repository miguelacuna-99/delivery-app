package es.delivery.manager.auth.infrastructure.controller;

import es.delivery.manager.auth.application.service.InvalidCredentialsException;
import es.delivery.manager.auth.application.service.InvalidTokenException;
import es.delivery.manager.auth.application.service.LoginResult;
import es.delivery.manager.auth.application.usecase.ForgotPasswordUseCase;
import es.delivery.manager.auth.application.usecase.LoginUseCase;
import es.delivery.manager.auth.application.usecase.ResetPasswordUseCase;
import es.delivery.manager.auth.application.usecase.ValidateTokenUseCase;
import es.delivery.manager.auth.domain.model.TokenClaims;
import es.delivery.manager.auth.infrastructure.mapper.UsuarioMapperImpl;
import es.delivery.manager.contracts.model.TipoUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(UsuarioMapperImpl.class)
@TestPropertySource(properties = "platform.api-key=clave-de-plataforma")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginUseCase loginUseCase;
    @MockBean
    private ValidateTokenUseCase validateTokenUseCase;
    @MockBean
    private ForgotPasswordUseCase forgotPasswordUseCase;
    @MockBean
    private ResetPasswordUseCase resetPasswordUseCase;

    @Test
    void elLoginDevuelveTokenTipoYMustChangePassword() throws Exception {
        when(loginUseCase.login("root.demo", "Root-1234"))
                .thenReturn(new LoginResult("jwt-de-prueba", TipoUsuario.ROOT, true));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"root.demo\", \"password\": \"Root-1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-de-prueba"))
                .andExpect(jsonPath("$.tipo").value("ROOT"))
                .andExpect(jsonPath("$.mustChangePassword").value(true));
    }

    @Test
    void unasCredencialesMalasSeTraducenEn401() throws Exception {
        when(loginUseCase.login(anyString(), anyString()))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"root.demo\", \"password\": \"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void validarUnTokenBuenoDevuelveLosClaims() throws Exception {
        when(validateTokenUseCase.validate("jwt-bueno")).thenReturn(TokenClaims.builder()
                .userId("user-1").username("admin.demo").comercioId("comercio-1")
                .tipo(TipoUsuario.ADMIN).build());

        mockMvc.perform(post("/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"jwt-bueno\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.comercioId").value("comercio-1"))
                .andExpect(jsonPath("$.tipo").value("ADMIN"));
    }

    @Test
    void validarUnTokenMaloDevuelve200ConValidFalse() throws Exception {
        // No es un 401: /auth/validate siempre responde 200 y deja decidir al llamante
        when(validateTokenUseCase.validate(anyString())).thenThrow(new InvalidTokenException());

        mockMvc.perform(post("/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"jwt-caducado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void olvidoDeContrasenaSiempreResponde204() throws Exception {
        // Aunque el correo no exista: distinguir permitiria enumerar cuentas
        mockMvc.perform(post("/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mail\": \"no.existe@example.com\"}"))
                .andExpect(status().isNoContent());

        verify(forgotPasswordUseCase).requestReset("no.existe@example.com");
    }
}
