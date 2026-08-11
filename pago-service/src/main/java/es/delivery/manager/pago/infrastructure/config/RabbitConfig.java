package es.delivery.manager.pago.infrastructure.config;

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

    public static final String PAGO_SOLICITADO_QUEUE = "pago.solicitado.queue";
    public static final String DEVOLUCION_SOLICITADA_QUEUE = "pago.devolucion-solicitada.queue";

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(RoutingKeys.EXCHANGE, true, false);
    }

    @Bean
    public Queue pagoSolicitadoQueue() {
        return new Queue(PAGO_SOLICITADO_QUEUE, true);
    }

    @Bean
    public Queue devolucionSolicitadaQueue() {
        return new Queue(DEVOLUCION_SOLICITADA_QUEUE, true);
    }

    @Bean
    public Binding pagoSolicitadoBinding(Queue pagoSolicitadoQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(pagoSolicitadoQueue).to(deliveryExchange).with(RoutingKeys.PAGO_SOLICITADO);
    }

    @Bean
    public Binding devolucionSolicitadaBinding(Queue devolucionSolicitadaQueue, TopicExchange deliveryExchange) {
        return BindingBuilder.bind(devolucionSolicitadaQueue).to(deliveryExchange).with(RoutingKeys.DEVOLUCION_SOLICITADA);
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
