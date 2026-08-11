package es.delivery.manager.auth.infrastructure.mail;

import es.delivery.manager.auth.domain.service.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adaptador mock de correo: registra el envio en el log.
 * En produccion se sustituye por un adaptador SMTP real sin tocar
 * dominio ni casos de uso (mismo puerto EmailPort).
 */
@Slf4j
@Component
public class LoggingEmailAdapter implements EmailPort {

    @Override
    public void sendPasswordResetEmail(String mail, String resetUrl) {
        log.info("[MOCK EMAIL] Para: {} — Restablece tu contrasena en: {}", mail, resetUrl);
    }
}
