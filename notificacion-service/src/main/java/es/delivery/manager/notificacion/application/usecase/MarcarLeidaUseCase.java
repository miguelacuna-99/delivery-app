package es.delivery.manager.notificacion.application.usecase;

import es.delivery.manager.notificacion.domain.model.Notificacion;
import es.delivery.manager.notificacion.domain.model.TokenClaims;

public interface MarcarLeidaUseCase {
    Notificacion marcarLeida(TokenClaims caller, String notificacionId);
}
