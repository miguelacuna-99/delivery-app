package es.delivery.manager.auth.domain.model;

import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Usuario de comercio: ROOT / ADMIN / PERSONAL / REPARTIDOR.
 * Siempre vinculado a un comercio mediante comercioId (multi-tenant).
 * El usuario ROOT lo provisiona la plataforma al dar de alta el comercio;
 * en su primer acceso debe cambiar la contrasena via correo (mustChangePassword).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    private String id;
    private String comercioId;
    private String username;
    private String passwordHash;
    private String mail;
    private String telefono;
    private TipoUsuario tipo;
    private boolean mustChangePassword;
}
