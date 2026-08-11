package es.delivery.manager.pedido.domain.service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Consulta sincrona a fidelidad-service en el checkout: porcentaje del cupon
 * (vacio si no es usable por ese cliente) y saldo de puntos. La reserva y
 * consolidacion real de puntos/cupon es asincrona via eventos pedido.*.
 */
public interface FidelidadPort {
    Optional<BigDecimal> porcentajeCupon(String codigo, String comercioId, String clienteId);
    int saldoPuntos(String clienteId);
}
