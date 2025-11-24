# 🚀 Guía de Despliegue en VPS con Traefik

## Configuración DNS Requerida

Antes de desplegar, configura estos registros DNS en tu proveedor de dominios para `pnaltsw.site`:

```
A       pnaltsw.site           → IP_DE_TU_VPS
A       www.pnaltsw.site       → IP_DE_TU_VPS
A       api.pnaltsw.site       → IP_DE_TU_VPS
A       grafana.pnaltsw.site   → IP_DE_TU_VPS
A       prometheus.pnaltsw.site → IP_DE_TU_VPS
A       rabbitmq.pnaltsw.site  → IP_DE_TU_VPS
A       traefik.pnaltsw.site   → IP_DE_TU_VPS
```

## Pasos de Despliegue

### 1. Preparar el VPS

En tu droplet de Digital Ocean:

```bash
# Actualizar el sistema
sudo apt update && sudo apt upgrade -y

# Instalar Git si no está instalado
sudo apt install git -y

# Clonar el repositorio
git clone <URL_DEL_REPOSITORIO>
cd reto2_Sistema-de-Control-de-Acceso
```

### 2. Configurar Variables de Entorno

```bash
# Copiar el archivo de ejemplo
cp .env.example .env

# Generar hash para el dashboard de Traefik
chmod +x generate-password.sh
./generate-password.sh

# Editar con tus credenciales (incluir el hash generado)
nano .env
```

**Variables importantes a configurar:**
- `MYSQL_ROOT_PASSWORD`: Password seguro para MySQL
- `MYSQL_PASSWORD`: Password para el usuario de la aplicación
- `MONGO_ROOT_PASSWORD`: Password para MongoDB
- `RABBITMQ_PASSWORD`: Password para RabbitMQ
- `GRAFANA_PASSWORD`: Password para Grafana
- `LETSENCRYPT_EMAIL`: Tu email real para Let's Encrypt
- `TRAEFIK_DASHBOARD_USER`: Usuario del dashboard de Traefik
- `TRAEFIK_DASHBOARD_PASSWORD_HASH`: Hash de la contraseña (usa `./generate-password.sh`)

### 3. Ejecutar el Despliegue

```bash
# Dar permisos de ejecución
chmod +x deploy.sh

# Ejecutar el script de despliegue
./deploy.sh
```

El script automáticamente:
- ✅ Instala Docker y Docker Compose
- ✅ Configura los certificados SSL
- ✅ Construye todas las imágenes
- ✅ Despliega todos los servicios
- ✅ Configura Traefik como reverse proxy

### 4. Verificar el Despliegue

```bash
# Ver el estado de todos los contenedores
docker-compose -f docker-compose.prod.yml ps

# Ver logs en tiempo real
docker-compose -f docker-compose.prod.yml logs -f

# Ver logs de un servicio específico
docker-compose -f docker-compose.prod.yml logs -f traefik
```

## URLs Disponibles

Una vez desplegado, tendrás acceso a:

- **🏠 Aplicación Principal**: https://pnaltsw.site
- **🔌 API Gateway**: https://api.pnaltsw.site
- **📊 Grafana**: https://grafana.pnaltsw.site
- **📈 Prometheus**: https://prometheus.pnaltsw.site  
- **🐰 RabbitMQ**: https://rabbitmq.pnaltsw.site
- **🔀 Traefik Dashboard**: https://traefik.pnaltsw.site

## Características del Despliegue

### ✨ Configuración Actualizada (Traefik v3.6)
- **Traefik v3.6**: Versión más reciente con todas las características modernas
- **TLS Challenge optimizado**: Configuración mejorada para certificados SSL
- **Regex actualizada**: Compatibilidad con la nueva sintaxis de Traefik v3
- **Logging mejorado**: Configuración de logs más detallada

### 🔒 SSL Automático
- Certificados SSL automáticos con Let's Encrypt
- Renovación automática de certificados
- Redirección automática HTTP → HTTPS con prioridad optimizada

### 🔄 Load Balancing
- Traefik maneja automáticamente el balanceo de carga
- Descubrimiento automático de servicios
- Health checks integrados

### 📊 Monitoreo
- Prometheus para métricas
- Grafana para visualización
- RabbitMQ Management UI
- Traefik Dashboard

### 🛡️ Seguridad
- Passwords seguros en variables de entorno
- Autenticación básica para dashboards administrativos
- Red interna Docker para comunicación entre servicios

## Comandos Útiles

### Gestión de Servicios
```bash
# Reiniciar todos los servicios
docker-compose -f docker-compose.prod.yml restart

# Reiniciar un servicio específico
docker-compose -f docker-compose.prod.yml restart api-gateway

# Detener todo
docker-compose -f docker-compose.prod.yml down

# Actualizar después de cambios en el código
docker-compose -f docker-compose.prod.yml up -d --build
```

### Monitoreo y Logs
```bash
# Ver recursos utilizados
docker stats

# Ver logs de errores
docker-compose -f docker-compose.prod.yml logs --tail=50 api-gateway

# Seguir logs en tiempo real
docker-compose -f docker-compose.prod.yml logs -f frontend
```

### Mantenimiento
```bash
# Limpiar imágenes no utilizadas
docker system prune -f

# Backup de volúmenes
docker run --rm -v reto2sistema-de-control-de-acceso_mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_backup.tar.gz /data

# Ver certificados SSL
docker exec traefik cat /letsencrypt/acme.json
```

## Troubleshooting

### Problema: Servicios no inician
```bash
# Verificar logs de un servicio específico
docker-compose -f docker-compose.prod.yml logs api-gateway

# Verificar health checks
docker inspect --format='{{.State.Health.Status}}' access-control-api-gateway
```

### Problema: SSL no funciona
```bash
# Verificar configuración de Traefik
docker-compose -f docker-compose.prod.yml logs traefik

# Verificar conectividad
curl -I http://pnaltsw.site
```

### Problema: Frontend no carga
```bash
# Verificar nginx en el contenedor frontend
docker exec access-control-frontend nginx -t

# Reconstruir solo el frontend
docker-compose -f docker-compose.prod.yml up -d --build frontend
```

## Actualizaciones

Para actualizar la aplicación:

```bash
# Obtener cambios del repositorio
git pull origin main

# Reconstruir y desplegar
docker-compose -f docker-compose.prod.yml up -d --build

# Opcional: limpiar imágenes antiguas
docker system prune -f
```

## Respaldo y Restauración

### Backup
```bash
# Crear backup de bases de datos
mkdir backups
docker exec access-control-mysql mysqldump -u root -p access_control_db > backups/mysql_backup.sql
docker exec access-control-mongodb mongodump --out /tmp/backup
docker cp access-control-mongodb:/tmp/backup ./backups/mongo_backup
```

### Restore
```bash
# Restaurar MySQL
docker exec -i access-control-mysql mysql -u root -p access_control_db < backups/mysql_backup.sql

# Restaurar MongoDB
docker cp ./backups/mongo_backup access-control-mongodb:/tmp/restore
docker exec access-control-mongodb mongorestore /tmp/restore
```
