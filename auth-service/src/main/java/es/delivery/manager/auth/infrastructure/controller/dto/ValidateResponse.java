package es.delivery.manager.auth.infrastructure.controller.dto;

import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateResponse {
    private boolean valid;
    private String userId;
    private String username;
    private String comercioId;
    private TipoUsuario tipo;

    public static ValidateResponse invalid() {
        return new ValidateResponse(false, null, null, null, null);
    }
}
