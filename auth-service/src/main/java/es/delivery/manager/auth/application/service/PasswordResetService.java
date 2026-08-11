package es.delivery.manager.auth.application.service;

import es.delivery.manager.auth.application.usecase.ForgotPasswordUseCase;
import es.delivery.manager.auth.application.usecase.ResetPasswordUseCase;
import es.delivery.manager.auth.domain.model.Cliente;
import es.delivery.manager.auth.domain.model.PasswordResetToken;
import es.delivery.manager.auth.domain.model.TipoCuenta;
import es.delivery.manager.auth.domain.model.Usuario;
import es.delivery.manager.auth.domain.repository.ClienteRepository;
import es.delivery.manager.auth.domain.repository.PasswordResetTokenRepository;
import es.delivery.manager.auth.domain.repository.UsuarioRepository;
import es.delivery.manager.auth.domain.service.EmailPort;
import es.delivery.manager.auth.domain.service.PasswordPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService implements ForgotPasswordUseCase, ResetPasswordUseCase {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordPort passwordPort;
    private final EmailPort emailPort;

    @Value("${app.reset-base-url}")
    private String resetBaseUrl;

    @Value("${app.reset-token-expiration-min}")
    private long resetTokenExpirationMin;

    @Override
    public void requestReset(String mail) {
        Optional<Usuario> usuario = usuarioRepository.findByMail(mail);
        Optional<Cliente> cliente = usuario.isPresent() ? Optional.empty() : clienteRepository.findByMail(mail);

        if (usuario.isEmpty() && cliente.isEmpty()) {
            // No revelar si el mail existe o no (evita enumeracion de cuentas)
            log.debug("Solicitud de reseteo para mail no registrado");
            return;
        }

        String userId = usuario.map(Usuario::getId).orElseGet(() -> cliente.get().getId());
        TipoCuenta tipoCuenta = usuario.isPresent() ? TipoCuenta.USUARIO : TipoCuenta.CLIENTE;

        // Un solo token vigente por cuenta
        tokenRepository.deleteByUserId(userId);

        String rawToken = generateRawToken();
        PasswordResetToken token = PasswordResetToken.builder()
                .userId(userId)
                .tipoCuenta(tipoCuenta)
                .mail(mail)
                .tokenHash(sha256(rawToken))
                .expiresAt(Instant.now().plus(resetTokenExpirationMin, ChronoUnit.MINUTES))
                .usado(false)
                .fechaCreacion(Instant.now())
                .build();
        tokenRepository.save(token);

        emailPort.sendPasswordResetEmail(mail, resetBaseUrl + "?token=" + rawToken);
    }

    @Override
    public void reset(String rawToken, String nuevaPassword) {
        PasswordResetToken token = tokenRepository.findByTokenHash(sha256(rawToken))
                .orElseThrow(InvalidResetTokenException::new);

        if (token.isUsado() || Instant.now().isAfter(token.getExpiresAt())) {
            throw new InvalidResetTokenException();
        }

        String hashed = passwordPort.encode(nuevaPassword);
        if (token.getTipoCuenta() == TipoCuenta.USUARIO) {
            Usuario usuario = usuarioRepository.findById(token.getUserId())
                    .orElseThrow(InvalidResetTokenException::new);
            usuario.setPasswordHash(hashed);
            usuario.setMustChangePassword(false);
            usuarioRepository.save(usuario);
        } else {
            Cliente cliente = clienteRepository.findById(token.getUserId())
                    .orElseThrow(InvalidResetTokenException::new);
            cliente.setPasswordHash(hashed);
            clienteRepository.save(cliente);
        }

        token.setUsado(true);
        tokenRepository.save(token);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
