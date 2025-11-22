package com.accesscontrol.saga.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración mejorada de RabbitMQ con Exchanges y Routing Keys
 */
@Configuration
public class RabbitMQConfig {
    
    // Exchanges
    public static final String SAGA_EXCHANGE = "saga.exchange";
    
    // Queues
    public static final String EMPLOYEE_SERVICE_QUEUE = "employee.service.queue";
    public static final String ACCESS_CONTROL_SERVICE_QUEUE = "access.control.service.queue";
    public static final String EMPLOYEE_SERVICE_DLQ = "employee.service.dlq";
    public static final String ACCESS_CONTROL_SERVICE_DLQ = "access.control.service.dlq";
    
    // Routing Keys
    public static final String ROUTING_KEY_EMPLOYEE_VALIDATE = "employee.validate";
    public static final String ROUTING_KEY_ACCESS_REGISTER = "access.register";
    public static final String ROUTING_KEY_ACCESS_COMPENSATE = "access.compensate";
    public static final String ROUTING_KEY_DLQ = "dlq";

    /**
     * Exchange principal para comunicación Saga
     */
    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SAGA_EXCHANGE, true, false);
    }

    /**
     * Queue para Employee Service
     */
    @Bean
    public Queue employeeServiceQueue() {
        return QueueBuilder.durable(EMPLOYEE_SERVICE_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", EMPLOYEE_SERVICE_DLQ)
                .build();
    }

    /**
     * Queue para Access Control Service
     */
    @Bean
    public Queue accessControlServiceQueue() {
        return QueueBuilder.durable(ACCESS_CONTROL_SERVICE_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ACCESS_CONTROL_SERVICE_DLQ)
                .build();
    }

    /**
     * Dead Letter Queue para Employee Service
     */
    @Bean
    public Queue employeeServiceDLQ() {
        return QueueBuilder.durable(EMPLOYEE_SERVICE_DLQ).build();
    }

    /**
     * Dead Letter Queue para Access Control Service
     */
    @Bean
    public Queue accessControlServiceDLQ() {
        return QueueBuilder.durable(ACCESS_CONTROL_SERVICE_DLQ).build();
    }

    /**
     * Binding Employee Service Queue a Saga Exchange
     */
    @Bean
    public Binding employeeServiceBinding() {
        return BindingBuilder
                .bind(employeeServiceQueue())
                .to(sagaExchange())
                .with(ROUTING_KEY_EMPLOYEE_VALIDATE);
    }

    /**
     * Binding Access Control Service Queue a Saga Exchange
     */
    @Bean
    public Binding accessControlServiceRegisterBinding() {
        return BindingBuilder
                .bind(accessControlServiceQueue())
                .to(sagaExchange())
                .with(ROUTING_KEY_ACCESS_REGISTER);
    }

    /**
     * Binding Access Control Service Queue para compensaciones
     */
    @Bean
    public Binding accessControlServiceCompensateBinding() {
        return BindingBuilder
                .bind(accessControlServiceQueue())
                .to(sagaExchange())
                .with(ROUTING_KEY_ACCESS_COMPENSATE);
    }

    /**
     * Message Converter JSON
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configurado
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setExchange(SAGA_EXCHANGE);
        template.setMandatory(true);
        return template;
    }

    /**
     * Listener Container Factory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}



