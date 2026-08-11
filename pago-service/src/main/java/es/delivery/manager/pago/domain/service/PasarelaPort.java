package es.delivery.manager.pago.domain.service;

import java.math.BigDecimal;

/**
 * Puerto hacia la pasarela de firma. Adaptador actual: mock que simula
 * la autorizacion del banco (infrastructure/pasarela).
 */
public interface PasarelaPort {

    ResultadoCobro cobrar(String pedidoId, BigDecimal importe);

    record ResultadoCobro(boolean autorizado, String firma) {
    }
}
