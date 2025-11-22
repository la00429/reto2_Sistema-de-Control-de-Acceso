# Refactorización de Métricas Prometheus

## Resumen

Se han agregado métricas personalizadas de Prometheus en el Access Control Service para monitoreo completo del sistema.

## Métricas Implementadas

### 1. Contadores (Counters)

#### `access_records_created_total`
- **Descripción**: Total de registros de acceso creados
- **Tipo**: Counter
- **Tags**: Ninguno
- **Uso**: Se incrementa cada vez que se crea un registro de acceso exitosamente

#### `access_records_by_type_total`
- **Descripción**: Total de registros de acceso por tipo (ENTRY/EXIT)
- **Tipo**: Counter
- **Tags**: `type` (entry/exit)
- **Uso**: Se incrementa con el tipo de acceso registrado

#### `access_validation_failed_total`
- **Descripción**: Total de validaciones de acceso fallidas
- **Tipo**: Counter
- **Tags**: `validation_type` (exit_without_entry, duplicate_entry, duplicate_exit, unknown)
- **Uso**: Se incrementa cuando una validación falla

#### `alerts_sent_total`
- **Descripción**: Total de alertas enviadas
- **Tipo**: Counter
- **Tags**: `alert_code` (EMPLOYEE_ALREADY_ENTERED, EMPLOYEE_ALREADY_LEFT, etc.)
- **Uso**: Se incrementa cada vez que se envía una alerta

### 2. Timers

#### `access_registration_duration_seconds`
- **Descripción**: Tiempo tomado para registrar un acceso
- **Tipo**: Timer
- **Uso**: Mide el tiempo total de ejecución del método `registerAccess`

#### `access_validation_duration_seconds`
- **Descripción**: Tiempo tomado para validar un acceso
- **Tipo**: Timer
- **Uso**: Mide el tiempo de ejecución del método `validateAccess`

#### `access_history_query_duration_seconds`
- **Descripción**: Tiempo tomado para consultar el historial de acceso
- **Tipo**: Timer
- **Uso**: Mide el tiempo de ejecución del método `getAllAccessHistory`

#### `employee_service_call_duration_seconds`
- **Descripción**: Tiempo tomado para llamar al servicio de empleados
- **Tipo**: Timer
- **Uso**: Mide el tiempo de llamadas al `EmployeeClient`

## Configuración

Las métricas se configuran en `MetricsConfig.java` y se inyectan en `AccessControlService.java`.

## Uso en Grafana

Estas métricas pueden ser visualizadas en Grafana usando queries como:

```promql
# Tasa de registros de acceso por minuto
rate(access_records_created_total[5m])

# Distribución de tipos de acceso
sum by (type) (access_records_by_type_total)

# Tasa de validaciones fallidas
rate(access_validation_failed_total[5m])

# Percentil 95 del tiempo de registro
histogram_quantile(0.95, rate(access_registration_duration_seconds_bucket[5m]))
```

## Próximos Pasos

1. Agregar métricas similares en otros servicios (Employee Service, Alert Service, etc.)
2. Crear dashboards específicos en Grafana para estas métricas
3. Configurar alertas en Prometheus para valores anómalos


