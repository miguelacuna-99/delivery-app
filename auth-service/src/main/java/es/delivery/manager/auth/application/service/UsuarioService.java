package es.delivery.manager.auth.application.service;

import es.delivery.manager.auth.application.usecase.CreateUsuarioUseCase;
import es.delivery.manager.auth.application.usecase.ForgotPasswordUseCase;
import es.delivery.manager.auth.application.usecase.ProvisionRootUseCase;
import es.delivery.manager.auth.domain.model.TokenClaims;
import es.delivery.manager.auth.domain.model.Usuario;
import es.delivery.manager.auth.domain.repository.ClienteRepository;
import es.delivery.manager.auth.domain.repository.UsuarioRepository;
import es.delivery.manager.auth.domain.service.PasswordPort;
import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService implements ProvisionRootUseCase, CreateUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordPort passwordPort;
    private final ForgotPasswordUseCase forgotPasswordUseCase;

    @Override
    public Usuario provisionRoot(Usuario root) {
        checkUsernameLibre(root.getUsername());

        root.setTipo(TipoUsuario.ROOT);
        // El ROOT llega con credenciales iniciales de la plataforma:
        // debe fijar su propia contrasena en el primer acceso
        root.setMustChangePassword(true);
        root.setPasswordHash(passwordPort.encode(root.getPasswordHash()));
        Usuario saved = usuarioRepository.save(root);

        // Correo de bienvenida con URL para fijar su contrasena
        forgotPasswordUseCase.requestReset(saved.getMail());
        return saved;
    }

    @Override
    public Usuario createUsuario(TokenClaims caller, Usuario usuario) {
        checkTipoPermitido(caller, usuario.getTipo());
        checkUsernameLibre(usuario.getUsername());

        // Multi-tenant: el usuario nuevo pertenece siempre al comercio del creador
        usuario.setComercioId(caller.getComercioId());
        usuario.setPasswordHash(passwordPort.encode(usuario.getPasswordHash()));
        usuario.setMustChangePassword(false);
        return usuarioRepository.save(usuario);
    }

    /**
     * Matriz de permisos de creacion de usuarios de comercio:
     * - ROOT: cualquier tipo de usuario de comercio (ROOT, ADMIN, PERSONAL, REPARTIDOR)
     * - ADMIN: solo PERSONAL y REPARTIDOR
     * - CLIENTE nunca se crea por aqui (se registra solo en /api/clientes/registro)
     */
    private void checkTipoPermitido(TokenClaims caller, TipoUsuario tipo) {
        if (tipo == null || tipo == TipoUsuario.CLIENTE) {
            throw new ForbiddenOperationException("Tipo de usuario no permitido: " + tipo);
        }
        boolean permitido = switch (caller.getTipo()) {
            case ROOT -> true;
            case ADMIN -> tipo == TipoUsuario.PERSONAL || tipo == TipoUsuario.REPARTIDOR;
            default -> false;
        };
        if (!permitido) {
            throw new ForbiddenOperationException(
                    "El tipo " + caller.getTipo() + " no puede crear usuarios de tipo " + tipo);
        }
    }

    private void checkUsernameLibre(String username) {
        if (usuarioRepository.findByUsername(username).isPresent()
                || clienteRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }
    }
}
