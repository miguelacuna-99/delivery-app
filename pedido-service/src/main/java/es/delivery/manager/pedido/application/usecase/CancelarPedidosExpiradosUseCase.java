package es.delivery.manager.pedido.application.usecase;

/**
 * Barrido de pedidos ACEPTADO cuyo plazo de pago ha vencido. Lo dispara un
 * proceso programado, no una peticion de usuario.
 */
public interface CancelarPedidosExpiradosUseCase {
    /** @return cuantos pedidos se han cancelado en esta pasada */
    int cancelarExpirados();
}
