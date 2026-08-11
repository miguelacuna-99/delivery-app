package es.delivery.manager.pedido.infrastructure.scheduler;

import es.delivery.manager.pedido.application.usecase.CancelarPedidosExpiradosUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara el barrido de pedidos con el plazo de pago vencido.
 *
 * Con varias instancias de pedido-service todas ejecutarian el barrido a la vez.
 * No pasa nada grave — cancelar es idempotente, solo actua sobre pedidos ACEPTADO —
 * pero si se despliega en varias replicas conviene un cerrojo distribuido
 * (ShedLock o equivalente) para no repetir trabajo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoTimeoutScheduler {

    private final CancelarPedidosExpiradosUseCase cancelarPedidosExpiradosUseCase;

    @Scheduled(
            initialDelayString = "${pedido.pago.timeout-check-ms:60000}",
            fixedDelayString = "${pedido.pago.timeout-check-ms:60000}")
    public void cancelarExpirados() {
        try {
            cancelarPedidosExpiradosUseCase.cancelarExpirados();
        } catch (RuntimeException e) {
            // Una excepcion que escape aqui detendria las siguientes ejecuciones
            log.error("Fallo el barrido de pedidos expirados", e);
        }
    }
}
