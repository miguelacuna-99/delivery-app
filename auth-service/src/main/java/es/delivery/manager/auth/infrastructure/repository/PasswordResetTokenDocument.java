package es.delivery.manager.auth.infrastructure.repository;

import es.delivery.manager.auth.domain.model.TipoCuenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "password_reset_tokens")
public class PasswordResetTokenDocument {
    @Id
    private String id;
    @Indexed
    private String userId;
    private TipoCuenta tipoCuenta;
    private String mail;
    @Indexed(unique = true)
    private String tokenHash;
    private Instant expiresAt;
    private boolean usado;
    private Instant fechaCreacion;
}
