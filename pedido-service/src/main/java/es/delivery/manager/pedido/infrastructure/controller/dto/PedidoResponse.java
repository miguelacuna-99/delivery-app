package es.delivery.manager.pedido.infrastructure.controller.dto;

import es.delivery.manager.contracts.model.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponse {
    private String id;
    private String numeroPedido;
    private String comercioId;
    private String clienteId;
    private List<ItemCarritoDto> items;
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
