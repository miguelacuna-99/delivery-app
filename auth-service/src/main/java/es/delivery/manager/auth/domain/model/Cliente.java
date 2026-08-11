package es.delivery.manager.auth.domain.model;

import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cliente final de la app. Se registra solo y puede modificar sus datos de contacto.
 * Su tipo es siempre CLIENTE. Cada web se vende a un unico comercio, asi que el
 * cliente queda asociado a ese comercio ya en el registro (comercioId obligatorio).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    private String id;
    private String comercioId;
    private String username;
    private String passwordHash;
    private String mail;
    private String direccionDomicilio;
    private String telefono;
    private TipoUsuario tipo;
}
