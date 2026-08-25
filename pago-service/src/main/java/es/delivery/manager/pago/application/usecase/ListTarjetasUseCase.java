package es.delivery.manager.pago.application.usecase;

import es.delivery.manager.pago.domain.model.TokenClaims;
import es.delivery.manager.pago.domain.model.Tarjeta;

import java.util.List;

public interface ListTarjetasUseCase {
    List<Tarjeta> listMisTarjetas(TokenClaims caller);
}
