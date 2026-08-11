package es.delivery.manager.fidelidad.infrastructure.messaging;

import es.delivery.manager.contracts.event.PagoEventMessage;
import es.delivery.manager.contracts.event.PedidoEventMessage;
import es.delivery.manager.contracts.event.RoutingKeys;
import es.delivery.manager.fidelidad.application.usecase.ProcesarEventoPagoUseCase;
import es.delivery.manager.fidelidad.application.usecase.ProcesarEventoPedidoUseCase;
import es.delivery.manager.fidelidad.infrastructure.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume pedido.* (reserva, consolidacion y devolucion), pago.completado
 * (otorgar puntos) y devolucion.completada (retirar los puntos otorgados).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FidelidadEventListener {

    private final ProcesarEventoPedidoUseCase procesarEventoPedidoUseCase;
    private final ProcesarEventoPagoUseCase procesarEventoPagoUseCase;

    @RabbitListener(queues = RabbitConfig.PEDIDO_QUEUE)
    public void onPedidoEvent(PedidoEventMessage message) {
        log.debug("Recibido {} para pedido {}", message.getRoutingKey(), message.getPedidoId());
        switch (message.getRoutingKey()) {
            case RoutingKeys.PEDIDO_CREADO -> procesarEventoPedidoUseCase.pedidoCreado(message);
            case RoutingKeys.PEDIDO_ACEPTADO -> procesarEventoPedidoUseCase.pedidoAceptado(message);
            case RoutingKeys.PEDIDO_RECHAZADO -> procesarEventoPedidoUseCase.pedidoRechazado(message);
            case RoutingKeys.PEDIDO_CANCELADO -> procesarEventoPedidoUseCase.pedidoCancelado(message);
            // pedido.entregado no altera la fidelidad
            default -> log.debug("Evento {} sin efecto en fidelidad", message.getRoutingKey());
        }
    }

    @RabbitListener(queues = RabbitConfig.PAGO_QUEUE)
    public void onPagoCompletado(PagoEventMessage message) {
        log.debug("Recibido pago.completado para pedido {}", message.getPedidoId());
        procesarEventoPagoUseCase.pagoCompletado(message);
    }

    @RabbitListener(queues = RabbitConfig.DEVOLUCION_QUEUE)
    public void onDevolucionCompletada(PagoEventMessage message) {
        log.debug("Recibida devolucion.completada para pedido {}", message.getPedidoId());
        procesarEventoPagoUseCase.devolucionCompletada(message);
    }
}
