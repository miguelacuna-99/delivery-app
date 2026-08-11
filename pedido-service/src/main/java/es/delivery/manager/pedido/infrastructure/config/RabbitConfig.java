package es.delivery.manager.pedido.infrastructure.config;

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

    public static final String PAGO_RESULTADO_QUEUE = "pedido.pago-resultado.queue";
    public static final String DEVOLUCION_QUEUE = "pedido.devolucion.queue";
    public static final String COMERCIO_QUEUE = "pedido.comercio.queue";

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(RoutingKeys.EXCHANGE, true, false);
    }

    @Bean
    public Queue pagoResultadoQueue() {
        return new Queue(PAGO_RESULTADO_QUEUE, true);
    }

    @Bean
    public Queue devolucionQueue() {
        return new Queue(DEVOLUCION_QUEUE, true);
    }

    @Bean
    public Queue comercioQueue() {
        return new Queue(COMERCIO_QUEUE, true);
    }

    @Bean
    public Binding pagoCompletadoBinding(Queue pagoResultadoQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(pagoResultadoQueue).to(deliveryExchange).with(RoutingKeys.PAGO_COMPLETADO);
    }

    @Bean
    public Binding pagoFallidoBinding(Queue pagoResultadoQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(pagoResultadoQueue).to(deliveryExchange).with(RoutingKeys.PAGO_FALLIDO);
    }

    @Bean
    public Binding devolucionCompletadaBinding(Queue devolucionQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(devolucionQueue).to(deliveryExchange).with(RoutingKeys.DEVOLUCION_COMPLETADA);
    }

    // comercio.* es seguro como wildcard: todas sus claves son ComercioEventMessage
    @Bean
    public Binding comercioBinding(Queue comercioQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(comercioQueue).to(deliveryExchange).with(RoutingKeys.WILDCARD_COMERCIO);
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
