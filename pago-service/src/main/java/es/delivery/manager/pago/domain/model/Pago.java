package es.delivery.manager.pago.domain.model;

import es.delivery.manager.contracts.model.EstadoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Pago simulado por la pasarela mock. La firma imita la firma de una pasarela real.
 * La devolucion (anulacion tras pagar) tambien se simula aqui: estado DEVUELTO
 * cuando el "banco" confirma el ingreso al cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pago {
    private String id;
    private String pedidoId;
    private String comercioId;
    private String clienteId;
    private String tarjetaId;
    private BigDecimal importe;
    private String firma;
    private EstadoPago estado;
    private Instant fecha;
    private Instant fechaDevolucion;
}
