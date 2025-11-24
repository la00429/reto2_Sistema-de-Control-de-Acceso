# Guía de Monitoreo con Grafana

## ¿Qué es Grafana y para qué sirve?

**Grafana** es una herramienta de visualización y monitoreo que te permite:

1. **Visualizar métricas en tiempo real**: Ver gráficos y estadísticas de tus servicios mientras están corriendo
2. **Detectar problemas rápidamente**: Identificar cuellos de botella, errores o servicios que fallan
3. **Hacer análisis histórico**: Ver tendencias y patrones a lo largo del tiempo
4. **Crear alertas**: Recibir notificaciones cuando algo va mal
5. **Optimizar rendimiento**: Identificar servicios lentos o que consumen muchos recursos

## ¿Cómo funciona el monitoreo en este sistema?

### Flujo de Monitoreo

```
Servicios (Spring Boot) 
    ↓
Generan métricas (Prometheus Micrometer)
    ↓
Prometheus recolecta las métricas cada 15 segundos
    ↓
Grafana lee las métricas de Prometheus
    ↓
Muestra gráficos y dashboards visuales
```

### Componentes del Sistema de Monitoreo

1. **Prometheus**: Base de datos de métricas que recolecta y almacena datos
2. **Grafana**: Interfaz visual que muestra las métricas en dashboards
3. **Spring Boot Actuator**: Expone las métricas de cada servicio en el endpoint `/actuator/prometheus`

## Métricas que se Monitorean

### 1. Métricas de Salud de Servicios

- **Estado de cada servicio** (UP/DOWN)
- **Disponibilidad** de cada microservicio
- **Tiempo de respuesta** (latencia)

**¿Para qué sirve?**
- Saber si un servicio está caído
- Detectar servicios que se reinician constantemente
- Monitorear la disponibilidad general del sistema

### 2. Métricas de Rendimiento HTTP

- **Tasa de solicitudes** (requests por segundo)
- **Latencia HTTP** (tiempo de respuesta)
- **Errores HTTP** (4xx, 5xx)

**¿Para qué sirve?**
- Identificar picos de tráfico
- Detectar endpoints lentos
- Encontrar problemas de rendimiento

### 3. Métricas de Negocio (Access Control Service)

- **Registros de acceso creados**: Cuántos ingresos/salidas se registran
- **Registros por tipo**: ENTRY vs EXIT
- **Validaciones fallidas**: Intentos de acceso inválidos
- **Alertas enviadas**: Cantidad de alertas generadas

**¿Para qué sirve?**
- Ver patrones de uso (horarios pico, días con más actividad)
- Detectar intentos de acceso inválidos o sospechosos
- Analizar el volumen de operaciones

### 4. Métricas de SAGA (Transacciones Distribuidas)

- **Sagas iniciadas**: Cuántas transacciones se iniciaron
- **Sagas completadas**: Transacciones exitosas
- **Sagas compensadas**: Transacciones que tuvieron que revertirse
- **Sagas fallidas**: Transacciones que fallaron
- **Tiempo de ejecución**: Cuánto tiempo toma completar una Saga

**¿Para qué sirve?**
- Monitorear el flujo de transacciones distribuidas
- Detectar problemas en las transacciones
- Optimizar el tiempo de respuesta de las operaciones complejas

### 5. Métricas de RabbitMQ (Mensajería)

- **Mensajes en cola**: Cantidad de mensajes pendientes
- **Tasa de mensajes procesados**: Mensajes por segundo
- **Mensajes fallidos**: Mensajes que no se pudieron procesar
- **Dead Letter Queue**: Mensajes que fallaron y están en la cola de errores

**¿Para qué sirve?**
- Detectar acumulación de mensajes (cuellos de botella)
- Identificar problemas en el procesamiento asíncrono
- Monitorear la salud del sistema de mensajería

### 6. Métricas de Sistema (JVM)

- **Memoria usada**: Cuánta memoria RAM consume cada servicio
- **CPU**: Uso de procesador
- **Threads**: Número de hilos activos

**¿Para qué sirve?**
- Detectar fugas de memoria
- Identificar servicios que consumen muchos recursos
- Planificar escalamiento (necesidad de más recursos)

## Cómo Acceder a Grafana

### 1. Iniciar el Sistema

```bash
docker-compose up -d --build
```

### 2. Acceder a Grafana

- **URL**: http://localhost:3000
- **Usuario**: `admin`
- **Contraseña**: `adminpassword`

### 3. Dashboards Disponibles

Una vez dentro de Grafana, encontrarás 3 dashboards principales:

#### Dashboard: "Services Health Dashboard"
Muestra:
- Estado de salud de todos los servicios
- Tasa de solicitudes HTTP
- Latencia de respuesta (tiempo que tarda en responder)
- Uso de memoria JVM por servicio

**Cuándo usarlo:**
- Para ver el estado general del sistema
- Detectar servicios lentos o con problemas
- Monitorear el rendimiento en tiempo real

#### Dashboard: "SAGA Orchestrator Dashboard"
Muestra:
- Cantidad de Sagas iniciadas
- Estado de ejecución (completadas, compensadas, fallidas)
- Tiempo de ejecución de las Sagas

**Cuándo usarlo:**
- Para monitorear transacciones distribuidas
- Detectar problemas en el orquestador
- Analizar el rendimiento de las operaciones complejas

#### Dashboard: "RabbitMQ Dashboard"
Muestra:
- Mensajes en cola
- Tasa de procesamiento
- Mensajes en Dead Letter Queue
- Estado de las colas

**Cuándo usarlo:**
- Para detectar acumulación de mensajes
- Monitorear el sistema de mensajería asíncrona
- Identificar problemas de procesamiento

## Ejemplos Prácticos de Uso

### Escenario 1: Detectando un Servicio Caído

1. Abres Grafana → Dashboard "Services Health Dashboard"
2. Ves que "Access Control Service" está en rojo (DOWN)
3. **Acción**: Revisar logs del servicio
   ```bash
   docker-compose logs -f access-control-service
   ```

### Escenario 2: Identificando Lentitud

1. Abres Grafana → Dashboard "Services Health Dashboard"
2. Ves que la latencia HTTP está muy alta (> 2 segundos)
3. **Acción**: Revisar qué endpoints son lentos y optimizarlos

### Escenario 3: Analizando Patrones de Uso

1. Abres Grafana → Dashboard con métricas de negocio
2. Ves que hay un pico de "access_records_created_total" a las 8 AM
3. **Acción**: Preparar más recursos para ese horario pico

### Escenario 4: Detectando Problemas en Transacciones

1. Abres Grafana → Dashboard "SAGA Orchestrator Dashboard"
2. Ves que hay muchas "Sagas compensadas" (revertidas)
3. **Acción**: Investigar por qué las transacciones fallan

### Escenario 5: Monitoreando Mensajería

1. Abres Grafana → Dashboard "RabbitMQ Dashboard"
2. Ves que hay mensajes acumulados en la cola
3. **Acción**: Verificar si algún servicio consumidor está caído

## Configuración Técnica

### Prometheus (`monitoring/prometheus.yml`)

Prometheus está configurado para recolectar métricas cada 15 segundos de:
- API Gateway (puerto 8080)
- Login Service (puerto 8081)
- Employee Service (puerto 8082)
- Access Control Service (puerto 8083)
- Alert Service (puerto 8084)
- Auth Service (puerto 8085)
- Saga Service (puerto 8086)

### Métricas Personalizadas

El sistema incluye métricas personalizadas en `AccessControlService`:

```java
// Contadores
access_records_created_total              // Total de registros creados
access_records_by_type_total              // Registros por tipo (entry/exit)
access_validation_failed_total            // Validaciones fallidas
alerts_sent_total                         // Alertas enviadas

// Timers (tiempo de ejecución)
access_registration_duration_seconds      // Tiempo de registro
access_validation_duration_seconds        // Tiempo de validación
access_history_query_duration_seconds     // Tiempo de consulta de historial
employee_service_call_duration_seconds    // Tiempo de llamada a Employee Service
```

## Acceso Rápido

### URLs Importantes

- **Grafana**: http://localhost:3000 (admin/adminpassword)
- **Prometheus**: http://localhost:9090
- **RabbitMQ Management**: http://localhost:15673 (admin/adminpassword)

### Consultas de Ejemplo en Grafana

**Ver registros de acceso creados:**
```
access_records_created_total
```

**Ver registros por tipo:**
```
access_records_by_type_total{type="entry"}
access_records_by_type_total{type="exit"}
```

**Ver tiempo promedio de registro:**
```
rate(access_registration_duration_seconds_sum[5m]) / rate(access_registration_duration_seconds_count[5m])
```

**Ver validaciones fallidas:**
```
access_validation_failed_total
```

## Resumen

**Grafana te permite:**
- ✅ Ver en tiempo real qué está pasando con tus servicios
- ✅ Detectar problemas antes de que afecten a los usuarios
- ✅ Analizar tendencias y patrones de uso
- ✅ Tomar decisiones basadas en datos reales
- ✅ Optimizar el rendimiento del sistema

**Es esencial para:**
- Monitoreo proactivo (detectar problemas antes de que ocurran)
- Análisis de rendimiento (encontrar cuellos de botella)
- Troubleshooting (diagnosticar problemas rápidamente)
- Capacidad de planificación (saber cuándo escalar recursos)

