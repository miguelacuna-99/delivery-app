package es.delivery.manager.pedido.infrastructure.repository;

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
@Document(collection = "estados_comercio")
public class EstadoComercioDocument {
    @Id
    private String id;
    @Indexed(unique = true)
    private String comercioId;
    private boolean operativo;
    private Instant fechaActualizacion;
}
