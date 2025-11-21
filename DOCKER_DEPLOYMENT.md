# Guía de Despliegue con Docker

## Descripción General

Este sistema de control de acceso está completamente dockerizado. Todos los servicios de Spring Boot, bases de datos y herramientas de monitoreo se ejecutan en contenedores Docker dentro de una red bridge compartida, lo que evita problemas de CORS y facilita la comunicación entre servicios.

## Arquitectura

### Servicios Backend (Dockerizados):
- **API Gateway** (puerto 8080) - Punto de entrada único para todas las peticiones
- **Login Service** (puerto 8081) - Gestión de autenticación
- **Employee Service** (puerto 8082) - Gestión de empleados
- **Access Control Service** (puerto 8083) - Control de accesos
- **Alert Service** (puerto 8084) - Sistema de alertas
- **Auth Service** (puerto 8085) - Servicio de autenticación JWT
- **Saga Service** (puerto 8086) - Orquestador de transacciones distribuidas

### Infraestructura (Dockerizada):
- **MySQL** (puerto 3307) - Base de datos relacional
- **MongoDB** (puerto 27018) - Base de datos NoSQL
- **RabbitMQ** (puertos 5673, 15673) - Message broker
- **Prometheus** (puerto 9090) - Sistema de métricas
- **Grafana** (puerto 3000) - Visualización de métricas

### Frontend (No Dockerizado):
- **React + Vite** (puerto 5173) - Interfaz de usuario

## Requisitos Previos

- Docker (versión 20.10+)
- Docker Compose (versión 2.0+)
- Node.js y npm (para el frontend)

## Despliegue Completo

### 1. Iniciar todos los contenedores

Desde el directorio raíz del proyecto, ejecuta:

```bash
docker-compose up -d --build
```

Este comando:
- Construye las imágenes Docker de todos los servicios de Spring
- Inicia todos los contenedores en modo detached
- Configura la red bridge `access-control-network`
- Aplica healthchecks para asegurar que los servicios estén listos

### 2. Verificar el estado de los contenedores

```bash
docker-compose ps
```

Todos los servicios deben mostrar el estado `healthy` después de aproximadamente 1-2 minutos.

### 3. Ver logs de los servicios

Para ver los logs de todos los servicios:

```bash
docker-compose logs -f
```

Para ver los logs de un servicio específico:

```bash
docker-compose logs -f api-gateway
docker-compose logs -f login-service
docker-compose logs -f employee-service
# etc.
```

### 4. Iniciar el Frontend

En una terminal separada, navega al directorio del frontend:

```bash
cd frontend
npm install
npm run dev
```

El frontend estará disponible en: http://localhost:5173

## URLs de Acceso

### Aplicación
- **Frontend**: http://localhost:5173
- **API Gateway**: http://localhost:8080

### Swagger UI (Documentación de APIs)
- **API Gateway**: http://localhost:8080/swagger-ui.html
- **Login Service**: http://localhost:8081/swagger-ui.html
- **Employee Service**: http://localhost:8082/swagger-ui.html
- **Access Control Service**: http://localhost:8083/swagger-ui.html
- **Alert Service**: http://localhost:8084/swagger-ui.html
- **Auth Service**: http://localhost:8085/swagger-ui.html
- **Saga Service**: http://localhost:8086/swagger-ui.html

### Monitoreo
- **Grafana**: http://localhost:3000 (admin/adminpassword)
- **Prometheus**: http://localhost:9090
- **RabbitMQ Management**: http://localhost:15673 (admin/adminpassword)

## Comandos Útiles

### Detener todos los contenedores

```bash
docker-compose down
```

### Detener y eliminar volúmenes (limpieza completa)

```bash
docker-compose down -v
```

### Reconstruir un servicio específico

```bash
docker-compose up -d --build login-service
```

### Ver recursos utilizados

```bash
docker stats
```

### Acceder a un contenedor

```bash
docker exec -it access-control-api-gateway sh
docker exec -it access-control-mysql mysql -uappuser -papppassword access_control_db
docker exec -it access-control-mongodb mongosh -u admin -p adminpassword
```

## Configuración de Red

Todos los servicios están conectados a la red bridge `access-control-network`. Esto permite:

1. **Comunicación interna por nombre**: Los servicios se comunican entre sí usando sus nombres de contenedor (ej: `mysql`, `mongodb`, `rabbitmq`)
2. **Sin problemas de CORS**: Al estar en la misma red y usar el API Gateway como punto único de entrada
3. **Aislamiento**: La red está separada de otras aplicaciones Docker

## Variables de Entorno

Los servicios utilizan variables de entorno configuradas en el `docker-compose.yml`:

### Servicios con MySQL:
- `MYSQL_HOST=mysql`
- `MYSQL_PORT=3306`
- `MYSQL_USER=appuser`
- `MYSQL_PASSWORD=apppassword`

### Servicios con MongoDB:
- `MONGODB_HOST=mongodb`
- `MONGODB_PORT=27017`
- `MONGODB_USER=admin`
- `MONGODB_PASSWORD=adminpassword`

### Servicios con RabbitMQ:
- `RABBITMQ_HOST=rabbitmq`
- `RABBITMQ_PORT=5672`
- `RABBITMQ_USER=admin`
- `RABBITMQ_PASSWORD=adminpassword`

### API Gateway:
- `LOGIN_SERVICE_URL=http://login-service:8081`
- `AUTH_SERVICE_URL=http://auth-service:8085`
- `EMPLOYEE_SERVICE_URL=http://employee-service:8082`
- `ACCESS_CONTROL_SERVICE_URL=http://access-control-service:8083`
- `ALERT_SERVICE_URL=http://alert-service:8084`
- `SAGA_SERVICE_URL=http://saga-service:8086`

## Healthchecks

Cada servicio de Spring tiene un healthcheck configurado:

- **Intervalo**: 30 segundos
- **Timeout**: 10 segundos
- **Reintentos**: 5
- **Período de inicio**: 60 segundos

Los healthchecks utilizan el endpoint `/actuator/health` de Spring Boot Actuator.

## Dependencias entre Servicios

El `docker-compose.yml` está configurado con dependencias para asegurar el orden correcto de inicio:

1. **Bases de datos primero**: MySQL, MongoDB
2. **Message broker**: RabbitMQ
3. **Servicios base**: Auth Service
4. **Servicios de negocio**: Login, Employee, Access Control, Alert, Saga
5. **Gateway**: API Gateway (último en iniciar)

## Troubleshooting

### Los contenedores no inician correctamente

```bash
# Ver logs detallados
docker-compose logs -f [nombre-servicio]

# Verificar el estado de salud
docker-compose ps
```

### Error de puerto en uso

```bash
# Verificar qué está usando el puerto
sudo lsof -i :8080
sudo lsof -i :3307

# Detener el proceso o cambiar el puerto en docker-compose.yml
```

### Problemas de memoria

```bash
# Verificar uso de recursos
docker stats

# Aumentar memoria disponible para Docker en la configuración de Docker Desktop
```

### Reconstruir desde cero

```bash
# Detener todo y limpiar
docker-compose down -v
docker system prune -a

# Reconstruir
docker-compose up -d --build
```

### El frontend no se conecta al backend

1. Verificar que el API Gateway esté corriendo: http://localhost:8080/actuator/health
2. Verificar la configuración CORS en `api-gateway/src/main/java/com/accesscontrol/gateway/config/CorsConfig.java`
3. Verificar que el frontend apunte a http://localhost:8080 en `frontend/src/services/api.js`

## Desarrollo Local sin Docker

Si prefieres ejecutar servicios localmente para desarrollo:

1. Los archivos `application.yml` tienen valores por defecto para localhost
2. Usa los puertos mapeados:
   - MySQL: localhost:3307
   - MongoDB: localhost:27018
   - RabbitMQ: localhost:5673

3. Puedes ejecutar solo la infraestructura en Docker:

```bash
docker-compose up -d mysql mongodb rabbitmq prometheus grafana
```

Y luego ejecutar los servicios de Spring desde tu IDE.

## Monitoreo y Métricas

### Prometheus

Prometheus está configurado para recolectar métricas de todos los servicios de Spring:
- Accede a http://localhost:9090
- Usa queries como: `jvm_memory_used_bytes`, `http_server_requests_seconds_count`

### Grafana

1. Accede a http://localhost:3000
2. Login: admin/adminpassword
3. Los datasources ya están configurados para Prometheus
4. Importa o crea dashboards personalizados

## Backup y Restauración

### Backup de datos

```bash
# MySQL
docker exec access-control-mysql mysqldump -uappuser -papppassword access_control_db > backup_mysql.sql

# MongoDB
docker exec access-control-mongodb mongodump --username admin --password adminpassword --authenticationDatabase admin --db login_db --out /tmp/backup
docker cp access-control-mongodb:/tmp/backup ./backup_mongo
```

### Restauración

```bash
# MySQL
docker exec -i access-control-mysql mysql -uappuser -papppassword access_control_db < backup_mysql.sql

# MongoDB
docker cp ./backup_mongo access-control-mongodb:/tmp/backup
docker exec access-control-mongodb mongorestore --username admin --password adminpassword --authenticationDatabase admin --db login_db /tmp/backup/login_db
```

## Seguridad

⚠️ **IMPORTANTE**: Las contraseñas en este archivo son para desarrollo. En producción:

1. Usa variables de entorno externas o secretos de Docker
2. Cambia todas las contraseñas por defecto
3. Configura certificados SSL/TLS
4. Revisa las políticas de CORS
5. Configura firewalls y network policies adecuados

## Producción

Para despliegue en producción, considera:

1. **Usar imágenes optimizadas**: Las actuales son de desarrollo
2. **Configurar logs centralizados**: ELK Stack o similar
3. **Usar orquestadores**: Kubernetes en lugar de Docker Compose
4. **Configurar SSL/TLS**: En el API Gateway
5. **Implementar rate limiting**: Para proteger los servicios
6. **Configurar backup automático**: Para las bases de datos
7. **Usar secretos seguros**: Vault, AWS Secrets Manager, etc.

## Soporte

Para problemas o preguntas:
1. Revisa los logs: `docker-compose logs -f`
2. Verifica el estado: `docker-compose ps`
3. Revisa la documentación de Swagger de cada servicio

