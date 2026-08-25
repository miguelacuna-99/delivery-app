package es.delivery.manager.pago.infrastructure.repository;

import es.delivery.manager.pago.domain.model.MarcaTarjeta;
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
@Document(collection = "tarjetas")
public class TarjetaDocument {
    @Id
    private String id;
    @Indexed
    private String clienteId;
    private String titular;
    private MarcaTarjeta marca;
    private String ultimos4;
    private int mesExpiracion;
    private int anioExpiracion;
    private Instant fechaAlta;
}
