package es.delivery.manager.fidelidad.infrastructure.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cuentas_puntos")
@CompoundIndex(name = "cliente_comercio_unique", def = "{'clienteId': 1, 'comercioId': 1}", unique = true)
public class CuentaPuntosDocument {
    @Id
    private String id;
    @Indexed
    private String clienteId;
    @Indexed
    private String comercioId;
    private int saldo;
    private List<MovimientoPuntosDocument> movimientos;
}
