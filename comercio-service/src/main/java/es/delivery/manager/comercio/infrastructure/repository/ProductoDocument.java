package es.delivery.manager.comercio.infrastructure.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "productos")
public class ProductoDocument {
    @Id
    private String id;
    @Indexed
    private String comercioId;
    private String nombre;
    private String descripcion;
    private String ingredientes;
    private String imagenUrl;
    private BigDecimal precio;
    private boolean disponible;
}
