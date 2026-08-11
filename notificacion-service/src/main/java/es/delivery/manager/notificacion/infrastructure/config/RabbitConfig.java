package es.delivery.manager.notificacion.infrastructure.config;

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

    public static final String PEDIDO_CREADO_QUEUE = "notificacion.pedido-creado.queue";
    public static final String PAGO_COMPLETADO_QUEUE = "notificacion.pago-completado.queue";

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(RoutingKeys.EXCHANGE, true, false);
    }

    @Bean
    public Queue pedidoCreadoQueue() {
        return new Queue(PEDIDO_CREADO_QUEUE, true);
    }

    @Bean
    public Queue pagoCompletadoQueue() {
        return new Queue(PAGO_COMPLETADO_QUEUE, true);
    }

    @Bean
    public Binding pedidoCreadoBinding(Queue pedidoCreadoQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(pedidoCreadoQueue).to(deliveryExchange).with(RoutingKeys.PEDIDO_CREADO);
    }

    @Bean
    public Binding pagoCompletadoBinding(Queue pagoCompletadoQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(pagoCompletadoQueue).to(deliveryExchange).with(RoutingKeys.PAGO_COMPLETADO);
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
