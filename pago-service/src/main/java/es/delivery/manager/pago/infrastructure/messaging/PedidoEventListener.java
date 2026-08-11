package es.delivery.manager.pago.infrastructure.messaging;

import es.delivery.manager.contracts.event.PedidoEventMessage;
import es.delivery.manager.pago.application.usecase.ProcesarDevolucionUseCase;
import es.delivery.manager.pago.application.usecase.ProcesarPagoUseCase;
import es.delivery.manager.pago.infrastructure.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume pago.solicitado (cobro) y devolucion.solicitada (ingreso al cliente).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoEventListener {

    private final ProcesarPagoUseCase procesarPagoUseCase;
    private final ProcesarDevolucionUseCase procesarDevolucionUseCase;

    @RabbitListener(queues = RabbitConfig.PAGO_SOLICITADO_QUEUE)
    public void onPagoSolicitado(PedidoEventMessage message) {
        log.debug("Recibido pago.solicitado para pedido {}", message.getPedidoId());
        procesarPagoUseCase.procesarPago(
                message.getPedidoId(),
                message.getComercioId(),
                message.getClienteId(),
                message.getTotal());
    }

    @RabbitListener(queues = RabbitConfig.DEVOLUCION_SOLICITADA_QUEUE)
    public void onDevolucionSolicitada(PedidoEventMessage message) {
        log.debug("Recibida devolucion.solicitada para pedido {}", message.getPedidoId());
        procesarDevolucionUseCase.procesarDevolucion(message.getPedidoId());
    }
}
