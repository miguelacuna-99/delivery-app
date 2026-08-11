package es.delivery.manager.auth.application.service;

import es.delivery.manager.auth.application.usecase.LoginUseCase;
import es.delivery.manager.auth.application.usecase.ValidateTokenUseCase;
import es.delivery.manager.auth.domain.model.Cliente;
import es.delivery.manager.auth.domain.model.TokenClaims;
import es.delivery.manager.auth.domain.model.Usuario;
import es.delivery.manager.auth.domain.repository.ClienteRepository;
import es.delivery.manager.auth.domain.repository.UsuarioRepository;
import es.delivery.manager.auth.domain.service.JwtPort;
import es.delivery.manager.auth.domain.service.PasswordPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService implements LoginUseCase, ValidateTokenUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final JwtPort jwtPort;
    private final PasswordPort passwordPort;

    @Override
    public LoginResult login(String username, String rawPassword) {
        // Primero usuarios de comercio, despues clientes
        Optional<Usuario> usuario = usuarioRepository.findByUsername(username);
        if (usuario.isPresent()) {
            return loginUsuario(usuario.get(), rawPassword);
        }

        Cliente cliente = clienteRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);
        return loginCliente(cliente, rawPassword);
    }

    @Override
    public TokenClaims validate(String token) {
        if (!jwtPort.isValid(token)) {
            throw new InvalidTokenException();
        }
        return jwtPort.extractClaims(token);
    }

    private LoginResult loginUsuario(Usuario usuario, String rawPassword) {
        if (!passwordPort.matches(rawPassword, usuario.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        TokenClaims claims = TokenClaims.builder()
                .userId(usuario.getId())
                .username(usuario.getUsername())
                .comercioId(usuario.getComercioId())
                .tipo(usuario.getTipo())
                .build();

        return LoginResult.builder()
                .token(jwtPort.generate(claims))
                .tipo(usuario.getTipo())
                .mustChangePassword(usuario.isMustChangePassword())
                .build();
    }

    private LoginResult loginCliente(Cliente cliente, String rawPassword) {
        if (!passwordPort.matches(rawPassword, cliente.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        TokenClaims claims = TokenClaims.builder()
                .userId(cliente.getId())
                .username(cliente.getUsername())
                .comercioId(cliente.getComercioId())
                .tipo(cliente.getTipo())
                .build();

        return LoginResult.builder()
                .token(jwtPort.generate(claims))
                .tipo(cliente.getTipo())
                .mustChangePassword(false)
                .build();
    }
}
