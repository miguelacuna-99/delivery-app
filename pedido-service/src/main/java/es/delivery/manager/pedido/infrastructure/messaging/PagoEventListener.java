package es.delivery.manager.pedido.infrastructure.messaging;

import es.delivery.manager.contracts.event.PagoEventMessage;
import es.delivery.manager.contracts.event.RoutingKeys;
import es.delivery.manager.pedido.application.usecase.RegistrarResultadoPagoUseCase;
import es.delivery.manager.pedido.infrastructure.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume los resultados de pago-service: pago.completado / pago.fallido
 * y devolucion.completada.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PagoEventListener {

    private final RegistrarResultadoPagoUseCase registrarResultadoPagoUseCase;

    @RabbitListener(queues = RabbitConfig.PAGO_RESULTADO_QUEUE)
    public void onPagoResultado(PagoEventMessage message) {
        log.debug("Recibido {} para pedido {}", message.getRoutingKey(), message.getPedidoId());
        switch (message.getRoutingKey()) {
            case RoutingKeys.PAGO_COMPLETADO ->
                    registrarResultadoPagoUseCase.pagoCompletado(message.getPedidoId());
            case RoutingKeys.PAGO_FALLIDO ->
                    registrarResultadoPagoUseCase.pagoFallido(message.getPedidoId());
            default -> log.warn("Routing key inesperada en cola de pagos: {}", message.getRoutingKey());
        }
    }

    @RabbitListener(queues = RabbitConfig.DEVOLUCION_QUEUE)
    public void onDevolucionCompletada(PagoEventMessage message) {
        log.debug("Recibida devolucion.completada para pedido {}", message.getPedidoId());
        registrarResultadoPagoUseCase.devolucionCompletada(message.getPedidoId());
    }
}
