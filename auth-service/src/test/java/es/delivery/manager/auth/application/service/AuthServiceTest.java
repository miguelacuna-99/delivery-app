package es.delivery.manager.auth.application.service;

import es.delivery.manager.auth.domain.model.Cliente;
import es.delivery.manager.auth.domain.model.TokenClaims;
import es.delivery.manager.auth.domain.model.Usuario;
import es.delivery.manager.auth.domain.repository.ClienteRepository;
import es.delivery.manager.auth.domain.repository.UsuarioRepository;
import es.delivery.manager.auth.domain.service.JwtPort;
import es.delivery.manager.auth.domain.service.PasswordPort;
import es.delivery.manager.contracts.model.TipoUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private JwtPort jwtPort;

    @Mock
    private PasswordPort passwordPort;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioRoot() {
        return Usuario.builder()
                .id("u1")
                .comercioId("c1")
                .username("root-pizzeria")
                .passwordHash("hash")
                .mail("root@pizzeria.es")
                .tipo(TipoUsuario.ROOT)
                .mustChangePassword(true)
                .build();
    }

    @Test
    void loginUsuarioDeComercioDevuelveTokenYMustChangePassword() {
        when(usuarioRepository.findByUsername("root-pizzeria")).thenReturn(Optional.of(usuarioRoot()));
        when(passwordPort.matches("secret", "hash")).thenReturn(true);
        when(jwtPort.generate(any(TokenClaims.class))).thenReturn("jwt-token");

        LoginResult result = authService.login("root-pizzeria", "secret");

        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getTipo()).isEqualTo(TipoUsuario.ROOT);
        assertThat(result.isMustChangePassword()).isTrue();
    }

    @Test
    void loginClienteDevuelveToken() {
        Cliente cliente = Cliente.builder()
                .id("cl1")
                .comercioId("c1")
                .username("ana")
                .passwordHash("hash")
                .tipo(TipoUsuario.CLIENTE)
                .build();
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.empty());
        when(clienteRepository.findByUsername("ana")).thenReturn(Optional.of(cliente));
        when(passwordPort.matches("secret", "hash")).thenReturn(true);
        when(jwtPort.generate(any(TokenClaims.class))).thenReturn("jwt-token");

        LoginResult result = authService.login("ana", "secret");

        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getTipo()).isEqualTo(TipoUsuario.CLIENTE);
        assertThat(result.isMustChangePassword()).isFalse();

        // El comercio del cliente tiene que viajar en el token, no quedarse null
        ArgumentCaptor<TokenClaims> claimsCaptor = ArgumentCaptor.forClass(TokenClaims.class);
        verify(jwtPort).generate(claimsCaptor.capture());
        assertThat(claimsCaptor.getValue().getComercioId()).isEqualTo("c1");
    }

    @Test
    void loginConPasswordIncorrectaLanzaInvalidCredentials() {
        when(usuarioRepository.findByUsername("root-pizzeria")).thenReturn(Optional.of(usuarioRoot()));
        when(passwordPort.matches("mala", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("root-pizzeria", "mala"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginConUsernameInexistenteLanzaInvalidCredentials() {
        when(usuarioRepository.findByUsername("nadie")).thenReturn(Optional.empty());
        when(clienteRepository.findByUsername("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("nadie", "secret"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void validateConTokenInvalidoLanzaInvalidToken() {
        when(jwtPort.isValid("malo")).thenReturn(false);

        assertThatThrownBy(() -> authService.validate("malo"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void validateConTokenValidoDevuelveClaims() {
        TokenClaims claims = TokenClaims.builder()
                .userId("u1").username("root-pizzeria").comercioId("c1").tipo(TipoUsuario.ROOT)
                .build();
        when(jwtPort.isValid(anyString())).thenReturn(true);
        when(jwtPort.extractClaims("bueno")).thenReturn(claims);

        assertThat(authService.validate("bueno")).isEqualTo(claims);
    }
}
