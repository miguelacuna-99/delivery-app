package es.delivery.manager.pedido.infrastructure.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carritos")
public class CarritoDocument {
    @Id
    private String id;
    @Indexed(unique = true)
    private String clienteId;
    private String comercioId;
    private List<ItemCarritoDocument> items;
    private String codigoCupon;
    private int puntosAplicados;
}
