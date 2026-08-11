package es.delivery.manager.fidelidad.infrastructure.config;

import es.delivery.manager.contracts.event.RoutingKeys;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String PEDIDO_QUEUE = "fidelidad.pedido.queue";
    public static final String PAGO_QUEUE = "fidelidad.pago.queue";
    public static final String DEVOLUCION_QUEUE = "fidelidad.devolucion.queue";

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(RoutingKeys.EXCHANGE, true, false);
    }

    @Bean
    public Queue fidelidadPedidoQueue() {
        return new Queue(PEDIDO_QUEUE, true);
    }

    @Bean
    public Queue fidelidadPagoQueue() {
        return new Queue(PAGO_QUEUE, true);
    }

    @Bean
    public Queue fidelidadDevolucionQueue() {
        return new Queue(DEVOLUCION_QUEUE, true);
    }

    @Bean
    public Binding fidelidadPedidoBinding(Queue fidelidadPedidoQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(fidelidadPedidoQueue).to(deliveryExchange).with(RoutingKeys.WILDCARD_PEDIDO);
    }

    // Binding explicito, no el wildcard pago.*: pago.solicitado viaja como
    // PedidoEventMessage y no deserializa en el PagoEventMessage del listener.
    // pago.fallido tampoco entra aqui: sus efectos llegan como pedido.cancelado,
    // que si trae el cupon y los puntos que hay que devolver.
    @Bean
    public Binding fidelidadPagoCompletadoBinding(Queue fidelidadPagoQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(fidelidadPagoQueue).to(deliveryExchange).with(RoutingKeys.PAGO_COMPLETADO);
    }

    @Bean
    public Binding fidelidadDevolucionBinding(Queue fidelidadDevolucionQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(fidelidadDevolucionQueue).to(deliveryExchange).with(RoutingKeys.DEVOLUCION_COMPLETADA);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
