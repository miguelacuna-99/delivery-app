package es.delivery.manager.pago.infrastructure.repository;

import es.delivery.manager.contracts.model.EstadoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pagos")
public class PagoDocument {
    @Id
    private String id;
    @Indexed(unique = true)
    private String pedidoId;
    @Indexed
    private String comercioId;
    @Indexed
    private String clienteId;
    private String tarjetaId;
    private BigDecimal importe;
    private String firma;
    private EstadoPago estado;
    private Instant fecha;
    private Instant fechaDevolucion;
}
