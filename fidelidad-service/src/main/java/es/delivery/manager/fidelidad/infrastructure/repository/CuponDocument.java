package es.delivery.manager.fidelidad.infrastructure.repository;

import es.delivery.manager.fidelidad.domain.model.EstadoCupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cupones")
public class CuponDocument {
    @Id
    private String id;
    @Indexed
    private String comercioId;
    @Indexed(unique = true)
    private String codigo;
    private BigDecimal porcentajeDescuento;
    private int usosMaximosPorUsuario;
    private List<UsoCuponDocument> usos;
    private EstadoCupon estado;
    private Instant fechaCaducidad;
}
