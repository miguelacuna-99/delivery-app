package es.delivery.manager.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Token de reseteo de contrasena. Se envia al correo del usuario dentro de una
 * URL cifrada; es de un solo uso y caduca en expiresAt.
 * Aplica tanto a usuarios de comercio (incluido ROOT) como a clientes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    private String id;
    private String userId;
    private TipoCuenta tipoCuenta;
    private String mail;
    private String tokenHash;
    private Instant expiresAt;
    private boolean usado;
    private Instant fechaCreacion;
}
