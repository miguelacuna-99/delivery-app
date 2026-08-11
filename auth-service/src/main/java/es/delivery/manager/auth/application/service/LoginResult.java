package es.delivery.manager.auth.application.service;

import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resultado del login: ademas del token, el tipo de usuario y si debe
 * cambiar la contrasena (primer acceso del ROOT provisionado).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult {
    private String token;
    private TipoUsuario tipo;
    private boolean mustChangePassword;
}
