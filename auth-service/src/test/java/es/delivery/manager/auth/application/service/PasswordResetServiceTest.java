package es.delivery.manager.auth.application.service;

import es.delivery.manager.auth.domain.model.Cliente;
import es.delivery.manager.auth.domain.model.PasswordResetToken;
import es.delivery.manager.auth.domain.model.TipoCuenta;
import es.delivery.manager.auth.domain.model.Usuario;
import es.delivery.manager.auth.domain.repository.ClienteRepository;
import es.delivery.manager.auth.domain.repository.PasswordResetTokenRepository;
import es.delivery.manager.auth.domain.repository.UsuarioRepository;
import es.delivery.manager.auth.domain.service.EmailPort;
import es.delivery.manager.auth.domain.service.PasswordPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordPort passwordPort;

    @Mock
    private EmailPort emailPort;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "resetBaseUrl", "http://localhost:3000/reset");
        ReflectionTestUtils.setField(passwordResetService, "resetTokenExpirationMin", 30L);
    }

    @Test
    void requestResetParaUsuarioGuardaTokenYEnviaCorreo() {
        Usuario usuario = Usuario.builder().id("u1").mail("root@pizzeria.es").build();
        when(usuarioRepository.findByMail("root@pizzeria.es")).thenReturn(Optional.of(usuario));

        passwordResetService.requestReset("root@pizzeria.es");

        verify(tokenRepository).deleteByUserId("u1");
        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTipoCuenta()).isEqualTo(TipoCuenta.USUARIO);
        assertThat(captor.getValue().getTokenHash()).isNotBlank();
        assertThat(captor.getValue().getExpiresAt()).isAfter(Instant.now());
        verify(emailPort).sendPasswordResetEmail(eq("root@pizzeria.es"), contains("?token="));
    }

    @Test
    void requestResetConMailDesconocidoNoHaceNada() {
        when(usuarioRepository.findByMail("nadie@x.es")).thenReturn(Optional.empty());
        when(clienteRepository.findByMail("nadie@x.es")).thenReturn(Optional.empty());

        passwordResetService.requestReset("nadie@x.es");

        verify(tokenRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(emailPort, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void resetConTokenCaducadoLanzaInvalidResetToken() {
        PasswordResetToken token = PasswordResetToken.builder()
                .userId("u1")
                .tipoCuenta(TipoCuenta.USUARIO)
                .tokenHash("hash")
                .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                .usado(false)
                .build();
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.reset("raw", "nueva"))
                .isInstanceOf(InvalidResetTokenException.class);
    }

    @Test
    void resetValidoDeClienteActualizaPasswordYMarcaUsado() {
        PasswordResetToken token = PasswordResetToken.builder()
                .userId("cl1")
                .tipoCuenta(TipoCuenta.CLIENTE)
                .tokenHash("hash")
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .usado(false)
                .build();
        Cliente cliente = Cliente.builder().id("cl1").passwordHash("vieja").build();
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(clienteRepository.findById("cl1")).thenReturn(Optional.of(cliente));
        when(passwordPort.encode("nueva")).thenReturn("nueva-hasheada");

        passwordResetService.reset("raw", "nueva");

        assertThat(cliente.getPasswordHash()).isEqualTo("nueva-hasheada");
        verify(clienteRepository).save(cliente);
        assertThat(token.isUsado()).isTrue();
        verify(tokenRepository).save(token);
    }
}
