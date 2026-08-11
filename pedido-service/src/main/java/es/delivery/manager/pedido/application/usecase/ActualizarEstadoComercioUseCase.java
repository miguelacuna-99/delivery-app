package es.delivery.manager.pedido.application.usecase;

/**
 * Mantiene al dia si un comercio admite pedidos, a partir de los eventos
 * comercio.suspendido / comercio.reactivado.
 */
public interface ActualizarEstadoComercioUseCase {
    void marcarSuspendido(String comercioId);
    void marcarOperativo(String comercioId);
}
