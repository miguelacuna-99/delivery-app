package es.delivery.manager.fidelidad.application.usecase;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Validacion sincrona de un cupon para un cliente (la llama pedido-service en
 * el checkout): devuelve el porcentaje de descuento si es usable, vacio si no.
 */
public interface ValidarCuponUseCase {
    Optional<BigDecimal> validarCupon(String codigo, String comercioId, String clienteId);
}
