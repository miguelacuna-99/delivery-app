package es.delivery.manager.pedido.domain.model;

import es.delivery.manager.contracts.model.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Pedido de un cliente. numeroPedido es un codigo corto que solo recibe el cliente;
 * el repartidor lo introduce para marcar la entrega.
 * Todas las transiciones de estado quedan fechadas para informes y facturacion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {
    private String id;
    private String numeroPedido;
    private String comercioId;
    private String clienteId;
    private List<ItemCarrito> items;
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
    private String tarjetaId;
    private Instant fechaCreacion;
    private Instant fechaAceptacion;
    private Instant fechaRechazo;
    private Instant fechaPago;
    private Instant fechaEntrega;
    private Instant fechaCancelacion;
    private Instant fechaAnulacion;
    private Instant fechaDevolucion;
}
