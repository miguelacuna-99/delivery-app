package es.delivery.manager.auth.infrastructure.controller.dto;

import es.delivery.manager.auth.application.service.LoginResult;
import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private TipoUsuario tipo;
    private boolean mustChangePassword;

    public static LoginResponse of(LoginResult result) {
        return new LoginResponse(result.getToken(), result.getTipo(), result.isMustChangePassword());
    }
}
