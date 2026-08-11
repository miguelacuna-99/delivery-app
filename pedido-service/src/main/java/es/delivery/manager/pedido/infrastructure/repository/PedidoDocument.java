package es.delivery.manager.pedido.infrastructure.repository;

import es.delivery.manager.contracts.model.EstadoPedido;
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
@Document(collection = "pedidos")
public class PedidoDocument {
    @Id
    private String id;
    @Indexed(unique = true)
    private String numeroPedido;
    @Indexed
    private String comercioId;
    @Indexed
    private String clienteId;
    private List<ItemCarritoDocument> items;
    private BigDecimal subtotal;
    private BigDecimal descuentoCupon;
    private BigDecimal descuentoPuntos;
    private BigDecimal total;
    private EstadoPedido estado;
    private Integer tiempoEstimadoMin;
    private String mensajeComercio;
    private String motivoCancelacion;
    private String motivoAnulacion;
    private String codigoCupon;
    private int puntosAplicados;
    private Instant fechaCreacion;
    private Instant fechaAceptacion;
    private Instant fechaRechazo;
    private Instant fechaPago;
    private Instant fechaEntrega;
    private Instant fechaCancelacion;
    private Instant fechaAnulacion;
    private Instant fechaDevolucion;
}
