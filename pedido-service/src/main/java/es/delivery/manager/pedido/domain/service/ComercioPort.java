package es.delivery.manager.pedido.domain.service;

import java.math.BigDecimal;

/**
 * Datos de configuracion del comercio consumidos en el checkout (aparte del
 * catalogo, que va por ProductoPort).
 */
public interface ComercioPort {
    BigDecimal getValorPunto(String comercioId);
}
