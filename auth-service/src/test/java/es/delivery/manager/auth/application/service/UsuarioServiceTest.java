package es.delivery.manager.auth.application.service;

import es.delivery.manager.auth.application.usecase.ForgotPasswordUseCase;
import es.delivery.manager.auth.domain.model.TokenClaims;
import es.delivery.manager.auth.domain.model.Usuario;
import es.delivery.manager.auth.domain.repository.ClienteRepository;
import es.delivery.manager.auth.domain.repository.UsuarioRepository;
import es.delivery.manager.auth.domain.service.PasswordPort;
import es.delivery.manager.contracts.model.TipoUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordPort passwordPort;

    @Mock
    private ForgotPasswordUseCase forgotPasswordUseCase;

    @InjectMocks
    private UsuarioService usuarioService;

    private TokenClaims caller(TipoUsuario tipo) {
        return TokenClaims.builder()
                .userId("caller-id")
                .username("caller")
                .comercioId("comercio-1")
                .tipo(tipo)
                .build();
    }

    private Usuario nuevoUsuario(TipoUsuario tipo) {
        return Usuario.builder()
                .username("nuevo")
                .passwordHash("raw-password")
                .mail("nuevo@comercio.es")
                .tipo(tipo)
                .build();
    }

    private void stubGuardado() {
        when(usuarioRepository.findByUsername("nuevo")).thenReturn(Optional.empty());
        when(clienteRepository.existsByUsername("nuevo")).thenReturn(false);
        when(passwordPort.encode(anyString())).thenReturn("hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ROOT puede crear usuarios de comercio de todos los tipos
    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"ROOT", "ADMIN", "PERSONAL", "REPARTIDOR"})
    void rootPuedeCrearTodosLosTiposDeUsuarioDeComercio(TipoUsuario tipo) {
        stubGuardado();

        Usuario creado = usuarioService.createUsuario(caller(TipoUsuario.ROOT), nuevoUsuario(tipo));

        assertThat(creado.getTipo()).isEqualTo(tipo);
        assertThat(creado.getComercioId()).isEqualTo("comercio-1");
    }

    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"PERSONAL", "REPARTIDOR"})
    void adminPuedeCrearPersonalYRepartidor(TipoUsuario tipo) {
        stubGuardado();

        Usuario creado = usuarioService.createUsuario(caller(TipoUsuario.ADMIN), nuevoUsuario(tipo));

        assertThat(creado.getTipo()).isEqualTo(tipo);
    }

    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"ROOT", "ADMIN"})
    void adminNoPuedeCrearRootNiAdmin(TipoUsuario tipo) {
        assertThatThrownBy(() -> usuarioService.createUsuario(caller(TipoUsuario.ADMIN), nuevoUsuario(tipo)))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(usuarioRepository, never()).save(any());
    }

    // Un CLIENTE que truque la llamada no puede crear usuarios de ningun tipo
    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"ROOT", "ADMIN", "PERSONAL", "REPARTIDOR", "CLIENTE"})
    void clienteNoPuedeCrearNingunUsuario(TipoUsuario tipo) {
        assertThatThrownBy(() -> usuarioService.createUsuario(caller(TipoUsuario.CLIENTE), nuevoUsuario(tipo)))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void nadieCreaClientesPorEsteEndpointNiSinTipo() {
        assertThatThrownBy(() -> usuarioService.createUsuario(caller(TipoUsuario.ROOT), nuevoUsuario(TipoUsuario.CLIENTE)))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThatThrownBy(() -> usuarioService.createUsuario(caller(TipoUsuario.ROOT), nuevoUsuario(null)))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(usuarioRepository, never()).save(any());
    }

    // El comercioId del request se ignora: siempre manda el del token (multi-tenant)
    @Test
    void elUsuarioCreadoPerteneceSiempreAlComercioDelCreador() {
        stubGuardado();
        Usuario intruso = nuevoUsuario(TipoUsuario.PERSONAL);
        intruso.setComercioId("otro-comercio");

        Usuario creado = usuarioService.createUsuario(caller(TipoUsuario.ROOT), intruso);

        assertThat(creado.getComercioId()).isEqualTo("comercio-1");
    }

    @Test
    void rootListaLosUsuariosDeSuComercio() {
        Usuario u1 = nuevoUsuario(TipoUsuario.PERSONAL);
        when(usuarioRepository.findByComercioId("comercio-1")).thenReturn(java.util.List.of(u1));

        var lista = usuarioService.listByComercio(caller(TipoUsuario.ROOT));

        assertThat(lista).containsExactly(u1);
    }

    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"ADMIN", "PERSONAL", "REPARTIDOR", "CLIENTE"})
    void soloRootPuedeListarUsuarios(TipoUsuario tipo) {
        assertThatThrownBy(() -> usuarioService.listByComercio(caller(tipo)))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void rootEliminaUnUsuarioDeSuComercio() {
        Usuario objetivo = nuevoUsuario(TipoUsuario.PERSONAL);
        objetivo.setId("usuario-2");
        objetivo.setComercioId("comercio-1");
        when(usuarioRepository.findById("usuario-2")).thenReturn(Optional.of(objetivo));

        usuarioService.deleteUsuario(caller(TipoUsuario.ROOT), "usuario-2");

        verify(usuarioRepository).deleteById("usuario-2");
    }

    @Test
    void rootNoPuedeEliminarseASiMismo() {
        assertThatThrownBy(() -> usuarioService.deleteUsuario(caller(TipoUsuario.ROOT), "caller-id"))
                .isInstanceOf(AutoEliminacionException.class);
        verify(usuarioRepository, never()).deleteById(anyString());
    }

    @Test
    void eliminarUsuarioDeOtroComercioSeTrataComoNotFound() {
        Usuario ajeno = nuevoUsuario(TipoUsuario.PERSONAL);
        ajeno.setId("usuario-3");
        ajeno.setComercioId("otro-comercio");
        when(usuarioRepository.findById("usuario-3")).thenReturn(Optional.of(ajeno));

        assertThatThrownBy(() -> usuarioService.deleteUsuario(caller(TipoUsuario.ROOT), "usuario-3"))
                .isInstanceOf(UsuarioNotFoundException.class);
        verify(usuarioRepository, never()).deleteById(anyString());
    }

    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"ADMIN", "PERSONAL", "REPARTIDOR", "CLIENTE"})
    void soloRootPuedeEliminarUsuarios(TipoUsuario tipo) {
        assertThatThrownBy(() -> usuarioService.deleteUsuario(caller(tipo), "usuario-2"))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(usuarioRepository, never()).deleteById(anyString());
    }

    @Test
    void provisionRootFuerzaTipoRootYMustChangePassword() {
        stubGuardado();
        Usuario root = nuevoUsuario(TipoUsuario.PERSONAL); // tipo del request se ignora
        root.setComercioId("comercio-1");

        Usuario creado = usuarioService.provisionRoot(root);

        assertThat(creado.getTipo()).isEqualTo(TipoUsuario.ROOT);
        assertThat(creado.isMustChangePassword()).isTrue();
        verify(forgotPasswordUseCase).requestReset("nuevo@comercio.es");
    }
}
