package com.accesscontrol.accesscontrol.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de métricas personalizadas para Prometheus
 */
@Configuration
public class MetricsConfig {
    
    /**
     * Contador de registros de acceso creados
     */
    @Bean
    public Counter accessRecordsCreatedCounter(MeterRegistry registry) {
        return Counter.builder("access_records_created_total")
                .description("Total number of access records created")
                .tag("type", "access")
                .register(registry);
    }
    
    /**
     * Método para crear contadores dinámicos con tags
     * Los contadores se crean bajo demanda con diferentes tags
     */
    public static Counter getAccessRecordsByTypeCounter(MeterRegistry registry, String type) {
        return Counter.builder("access_records_by_type_total")
                .description("Total number of access records by type")
                .tag("type", type)
                .register(registry);
    }
    
    /**
     * Método para crear contadores de validación fallida con tags
     */
    public static Counter getAccessValidationFailedCounter(MeterRegistry registry, String validationType) {
        return Counter.builder("access_validation_failed_total")
                .description("Total number of failed access validations")
                .tag("validation_type", validationType)
                .register(registry);
    }
    
    /**
     * Método para crear contadores de alertas con tags
     */
    public static Counter getAlertsSentCounter(MeterRegistry registry, String alertCode) {
        return Counter.builder("alerts_sent_total")
                .description("Total number of alerts sent")
                .tag("alert_code", alertCode)
                .register(registry);
    }
    
    /**
     * Timer para tiempo de registro de acceso
     */
    @Bean
    public Timer accessRegistrationTimer(MeterRegistry registry) {
        return Timer.builder("access_registration_duration_seconds")
                .description("Time taken to register an access record")
                .register(registry);
    }
    
    /**
     * Timer para tiempo de validación de acceso
     */
    @Bean
    public Timer accessValidationTimer(MeterRegistry registry) {
        return Timer.builder("access_validation_duration_seconds")
                .description("Time taken to validate an access record")
                .register(registry);
    }
    
    /**
     * Timer para tiempo de obtención de historial
     */
    @Bean
    public Timer accessHistoryQueryTimer(MeterRegistry registry) {
        return Timer.builder("access_history_query_duration_seconds")
                .description("Time taken to query access history")
                .register(registry);
    }
    
    /**
     * Timer para tiempo de llamada a Employee Service
     */
    @Bean
    public Timer employeeServiceCallTimer(MeterRegistry registry) {
        return Timer.builder("employee_service_call_duration_seconds")
                .description("Time taken to call employee service")
                .register(registry);
    }
}

