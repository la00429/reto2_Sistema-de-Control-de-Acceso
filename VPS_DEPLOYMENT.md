# 🚀 Guía de Despliegue en VPS (142.93.248.100)

## 1. Configuración DNS (IMPORTANTE - HACER PRIMERO)

Antes de comenzar el despliegue, configura estos registros DNS en tu proveedor de dominios:

```
Tipo  Nombre                   Valor            TTL
A     pnaltsw.site            142.93.248.100   300
A     www.pnaltsw.site        142.93.248.100   300
A     api.pnaltsw.site        142.93.248.100   300
A     traefik.pnaltsw.site    142.93.248.100   300
A     grafana.pnaltsw.site    142.93.248.100   300
A     prometheus.pnaltsw.site 142.93.248.100   300
A     rabbitmq.pnaltsw.site   142.93.248.100   300
```

⏰ **Espera 5-10 minutos** después de configurar el DNS antes de continuar.

## 2. Conectarse al VPS

```bash
ssh root@142.93.248.100
```

## 3. Preparar el Servidor

```bash
# Ejecutar el script de configuración
curl -fsSL https://raw.githubusercontent.com/TU_USUARIO/TU_REPO/main/setup-vps.sh | bash

# O copiar y ejecutar manualmente:
cd /opt
git clone TU_REPOSITORIO_URL access-control
cd access-control
chmod +x setup-vps.sh
./setup-vps.sh
```

## 4. Verificar Docker Compose

En tu VPS, verifica qué comando usar:

```bash
# Probar Docker Compose como plugin (recomendado)
docker compose version

# Si no funciona, probar como comando standalone
docker-compose --version

# Si ninguno funciona, instalar:
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
```

## 5. Configurar Variables de Entorno

```bash
# Copiar el archivo de ejemplo
cp .env.example .env

# Generar hash para Traefik Dashboard
./generate-traefik-password.sh

# Editar con tus credenciales
nano .env
```

### Variables importantes:
```bash
MYSQL_ROOT_PASSWORD=tu_password_mysql_muy_seguro
MYSQL_USER=appuser
MYSQL_PASSWORD=tu_password_app_muy_seguro
MONGO_ROOT_USER=admin
MONGO_ROOT_PASSWORD=tu_password_mongo_muy_seguro
RABBITMQ_USER=admin
RABBITMQ_PASSWORD=tu_password_rabbit_muy_seguro
GRAFANA_USER=admin
GRAFANA_PASSWORD=tu_password_grafana_muy_seguro
LETSENCRYPT_EMAIL=tu-email@ejemplo.com
TRAEFIK_DASHBOARD_AUTH=admin:$2y$10$hash_generado_por_script
```

## 6. Desplegar

```bash
# Hacer ejecutable el script
chmod +x deploy.sh

# Ejecutar despliegue
./deploy.sh
```

## 7. Verificar el Despliegue

### URLs disponibles:
- **🏠 Aplicación**: https://pnaltsw.site
- **🔌 API**: https://api.pnaltsw.site
- **📊 Grafana**: https://grafana.pnaltsw.site
- **📈 Prometheus**: https://prometheus.pnaltsw.site
- **🐰 RabbitMQ**: https://rabbitmq.pnaltsw.site
- **🔀 Traefik**: https://traefik.pnaltsw.site

### Comandos de verificación:
```bash
# Ver estado de contenedores
docker ps

# Ver logs de todos los servicios
docker compose -f docker-compose.prod.yml logs -f

# Ver logs específicos de Traefik
docker compose -f docker-compose.prod.yml logs -f traefik

# Ver certificados SSL
docker exec traefik cat /letsencrypt/acme.json | jq
```

## 8. Solución de Problemas

### Problema: "docker compose" no funciona
```bash
# Usar docker-compose en su lugar
docker-compose -f docker-compose.prod.yml up -d --build
```

### Problema: Certificados SSL fallan
```bash
# Verificar que el DNS esté propagado
nslookup pnaltsw.site
dig pnaltsw.site

# Ver logs de Traefik
docker logs traefik

# Reiniciar Traefik
docker restart traefik
```

### Problema: Frontend no carga
```bash
# Verificar que el API Gateway esté funcionando
curl http://localhost:8080/actuator/health

# Reiniciar frontend
docker compose -f docker-compose.prod.yml restart frontend
```

### Problema: Puertos ocupados
```bash
# Ver qué está usando los puertos 80/443
sudo netstat -tulpn | grep :80
sudo netstat -tulpn | grep :443

# Detener servicios conflictivos si es necesario
sudo systemctl stop apache2
sudo systemctl stop nginx
```

## 9. Mantenimiento

### Actualizar la aplicación:
```bash
cd /opt/access-control
git pull origin main
docker compose -f docker-compose.prod.yml up -d --build
```

### Reiniciar servicios:
```bash
# Reiniciar todo
docker compose -f docker-compose.prod.yml restart

# Reiniciar servicio específico
docker compose -f docker-compose.prod.yml restart api-gateway
```

### Backup:
```bash
# Crear backup de la base de datos
docker exec access-control-mysql mysqldump -u root -p access_control_db > backup_mysql.sql
docker exec access-control-mongodb mongodump --out /tmp/backup
docker cp access-control-mongodb:/tmp/backup ./backup_mongo
```

## 10. Seguridad Adicional

```bash
# Configurar firewall
ufw allow ssh
ufw allow 80
ufw allow 443
ufw --force enable

# Verificar estado
ufw status
```

## ⚠️ Notas Importantes

1. **DNS primero**: Siempre configura el DNS antes de desplegar
2. **Certificados**: Pueden tardar 2-5 minutos en generarse
3. **Docker Compose**: Usa el comando que funcione en tu sistema
4. **Logs**: Siempre revisa los logs si algo falla
5. **Firewall**: Asegúrate de que los puertos 80 y 443 estén abiertos
