package es.delivery.manager.pago.application.usecase;

import es.delivery.manager.pago.domain.model.TokenClaims;
import es.delivery.manager.pago.domain.model.Tarjeta;

/**
 * Solo el CLIENTE guarda tarjetas para si mismo.
 */
public interface GuardarTarjetaUseCase {
    Tarjeta guardarTarjeta(TokenClaims caller, String numero, String titular, int mesExpiracion, int anioExpiracion);
}
