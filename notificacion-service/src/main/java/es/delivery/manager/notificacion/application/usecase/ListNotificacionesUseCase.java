package es.delivery.manager.notificacion.application.usecase;

import es.delivery.manager.notificacion.domain.model.Notificacion;
import es.delivery.manager.notificacion.domain.model.TipoNotificacion;
import es.delivery.manager.notificacion.domain.model.TokenClaims;

import java.util.List;

/**
 * Bandejas del comercio (PEDIDOS PENDIENTES / PEDIDOS PAGADOS), sin leer.
 */
public interface ListNotificacionesUseCase {
    List<Notificacion> listNoLeidas(TokenClaims caller, TipoNotificacion tipo);
}
