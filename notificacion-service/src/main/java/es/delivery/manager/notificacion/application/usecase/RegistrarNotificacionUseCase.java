package es.delivery.manager.notificacion.application.usecase;

import es.delivery.manager.notificacion.domain.model.Notificacion;
import es.delivery.manager.notificacion.domain.model.TipoNotificacion;

/**
 * Crea una notificacion para el comercio al consumir pedido.creado
 * (PEDIDO_PENDIENTE) o pago.completado (PEDIDO_PAGADO).
 */
public interface RegistrarNotificacionUseCase {
    Notificacion registrar(String comercioId, TipoNotificacion tipo, String pedidoId, String numeroPedido);
}
