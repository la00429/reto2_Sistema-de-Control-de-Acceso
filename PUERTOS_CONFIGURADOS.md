# Configuración de Puertos

## Puertos Cambiados para Evitar Conflictos

Debido a que algunos servicios ya estaban corriendo en el sistema, se cambiaron los siguientes puertos:

### Bases de Datos
- **MySQL**: Puerto `3307` (originalmente 3306)
- **MongoDB**: Puerto `27018` (originalmente 27017)

### Mensajería
- **RabbitMQ**: Puerto `5673` (originalmente 5672)
- **RabbitMQ Management**: Puerto `15673` (originalmente 15672)

### Monitoreo (sin cambios)
- **Prometheus**: Puerto `9090`
- **Grafana**: Puerto `3000`

## Archivos Actualizados

### docker-compose.yml
- MySQL mapeado a puerto 3307
- MongoDB mapeado a puerto 27018
- RabbitMQ mapeado a puerto 5673
- RabbitMQ Management mapeado a puerto 15673

### application.yml de Servicios
- **login-service**: MongoDB puerto 27018, RabbitMQ puerto 5673
- **auth-service**: MongoDB puerto 27018
- **employee-service**: MySQL puerto 3307, RabbitMQ puerto 5673
- **access-control-service**: MySQL puerto 3307, RabbitMQ puerto 5673
- **alert-service**: MySQL puerto 3307, RabbitMQ puerto 5673
- **saga-service**: RabbitMQ puerto 5673

## Nota

Si prefieres usar los puertos originales, puedes:
1. Detener los servicios que están usando esos puertos
2. Revertir los cambios en docker-compose.yml y application.yml
3. O usar los puertos alternativos configurados





