package es.delivery.manager.auth.domain.model;

import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Claims del JWT. Los servicios usan comercioId para limitar cada operacion
 * al comercio del usuario (tambien en clientes, cada uno atado a un comercio).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenClaims {
    private String userId;
    private String username;
    private String comercioId;
    private TipoUsuario tipo;
}
