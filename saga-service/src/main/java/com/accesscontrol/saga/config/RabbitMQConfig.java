package com.accesscontrol.saga.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String EMPLOYEE_SERVICE_QUEUE = "employee.service.queue";
    public static final String ACCESS_CONTROL_SERVICE_QUEUE = "access.control.service.queue";

    @Bean
    public Queue employeeServiceQueue() {
        return new Queue(EMPLOYEE_SERVICE_QUEUE, true);
    }

    @Bean
    public Queue accessControlServiceQueue() {
        return new Queue(ACCESS_CONTROL_SERVICE_QUEUE, true);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}



