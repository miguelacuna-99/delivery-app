package es.delivery.manager.pago.application.usecase;

import es.delivery.manager.pago.domain.model.TokenClaims;

public interface EliminarTarjetaUseCase {
    void eliminarTarjeta(TokenClaims caller, String tarjetaId);
}
