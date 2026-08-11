package es.delivery.manager.pedido.application.service;

import es.delivery.manager.contracts.model.EstadoPedido;
import es.delivery.manager.pedido.application.usecase.CancelarPedidoUseCase;
import es.delivery.manager.pedido.application.usecase.CancelarPedidosExpiradosUseCase;
import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.domain.repository.PedidoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Un pedido ACEPTADO espera a que la pasarela responda. Si no responde nunca
 * (mensaje perdido, pago-service caido durante el corte), el pedido se quedaria
 * bloqueado para siempre reteniendo los puntos y el uso del cupon del cliente.
 * Este barrido lo cancela pasado el plazo y libera la reserva.
 */
@Slf4j
@Service
public class PedidoTimeoutService implements CancelarPedidosExpiradosUseCase {

    private final PedidoRepository pedidoRepository;
    private final CancelarPedidoUseCase cancelarPedidoUseCase;
    private final Duration plazoPago;

    public PedidoTimeoutService(PedidoRepository pedidoRepository,
                                CancelarPedidoUseCase cancelarPedidoUseCase,
                                @Value("${pedido.pago.timeout-min:15}") long timeoutMin) {
        this.pedidoRepository = pedidoRepository;
        this.cancelarPedidoUseCase = cancelarPedidoUseCase;
        this.plazoPago = Duration.ofMinutes(timeoutMin);
    }

    @Override
    public int cancelarExpirados() {
        Instant limite = Instant.now().minus(plazoPago);
        List<Pedido> expirados =
                pedidoRepository.findByEstadoAndFechaAceptacionBefore(EstadoPedido.ACEPTADO, limite);

        int cancelados = 0;
        for (Pedido pedido : expirados) {
            try {
                cancelarPedidoUseCase.cancelarPorFaltaDePago(pedido.getId(),
                        "Plazo de pago vencido (" + plazoPago.toMinutes() + " min)");
                cancelados++;
            } catch (RuntimeException e) {
                // Que uno falle no puede impedir que se procesen los demas
                log.error("No se pudo cancelar el pedido expirado {}: {}", pedido.getId(), e.getMessage());
            }
        }
        if (cancelados > 0) {
            log.info("Cancelados {} pedido(s) por plazo de pago vencido", cancelados);
        }
        return cancelados;
    }
}
