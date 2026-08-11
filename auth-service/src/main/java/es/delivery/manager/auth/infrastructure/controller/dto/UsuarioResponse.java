package es.delivery.manager.auth.infrastructure.controller.dto;

import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private String id;
    private String comercioId;
    private String username;
    private String mail;
    private String telefono;
    private TipoUsuario tipo;
    private boolean mustChangePassword;
}
