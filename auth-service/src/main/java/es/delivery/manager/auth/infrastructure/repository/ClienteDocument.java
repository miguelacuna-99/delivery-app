package es.delivery.manager.auth.infrastructure.repository;

import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clientes")
public class ClienteDocument {
    @Id
    private String id;
    @Indexed
    private String comercioId;
    @Indexed(unique = true)
    private String username;
    private String passwordHash;
    private String mail;
    private String direccionDomicilio;
    private String telefono;
    private TipoUsuario tipo;
}
