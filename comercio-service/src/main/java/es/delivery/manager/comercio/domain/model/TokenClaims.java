package es.delivery.manager.comercio.domain.model;

import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Identidad del llamante extraida del JWT (validado contra auth-service).
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
