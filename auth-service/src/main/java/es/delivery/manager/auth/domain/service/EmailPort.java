package es.delivery.manager.auth.domain.service;

/**
 * Puerto de envio de correos. Adaptador inicial: mock que registra el envio en log;
 * en produccion se sustituye por SMTP real sin tocar dominio ni aplicacion.
 */
public interface EmailPort {
    void sendPasswordResetEmail(String mail, String resetUrl);
}
