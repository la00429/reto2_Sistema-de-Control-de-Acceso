#!/bin/bash

# Script de despliegue para VPS con Traefik v3.6
echo "🚀 Iniciando despliegue del Sistema de Control de Acceso"
echo "🌐 Dominio: pnaltsw.site"
echo "📡 IP del VPS: 142.93.248.100"

# Verificar que Docker esté instalado
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado. Instalándolo..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
    sudo usermod -aG docker $USER
    echo "✅ Docker instalado. Por favor reinicia la sesión y ejecuta el script nuevamente."
    exit 1
fi

# Verificar que Docker Compose esté disponible
DOCKER_COMPOSE_CMD=""
if docker compose version &> /dev/null; then
    DOCKER_COMPOSE_CMD="docker compose"
    echo "✅ Usando Docker Compose plugin: $(docker compose version)"
elif command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE_CMD="docker-compose"
    echo "✅ Usando Docker Compose standalone: $(docker-compose --version)"
else
    echo "❌ Docker Compose no está instalado. Instalándolo..."
    sudo curl -L "https://github.com/docker/compose/releases/download/v2.23.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    DOCKER_COMPOSE_CMD="docker-compose"
fi

echo "📋 Verificando configuración..."

# Verificar que existe el archivo .env
if [ ! -f .env ]; then
    echo "❌ No se encontró el archivo .env"
    echo "📝 Copiando .env.example a .env..."
    cp .env.example .env
    echo "⚠️  IMPORTANTE: Configura el archivo .env con tus credenciales"
    echo "   - Cambia las contraseñas por unas seguras"
    echo "   - Verifica el email para Let's Encrypt"
    echo "   - Opcional: cambia el hash del dashboard de Traefik"
    echo ""
    echo "💡 Para generar un nuevo hash de contraseña para Traefik:"
    echo "   ./generate-traefik-password.sh"
    echo ""
    read -p "Presiona Enter cuando hayas configurado el archivo .env..."
fi

# Verificar configuración DNS
echo "🌐 Verificando configuración DNS..."
echo "Asegúrate de que estos registros DNS apunten a 142.93.248.100:"
echo "   A    pnaltsw.site           -> 142.93.248.100"
echo "   A    www.pnaltsw.site      -> 142.93.248.100"
echo "   A    api.pnaltsw.site      -> 142.93.248.100"
echo "   A    traefik.pnaltsw.site  -> 142.93.248.100"
echo "   A    grafana.pnaltsw.site  -> 142.93.248.100"
echo "   A    prometheus.pnaltsw.site -> 142.93.248.100"
echo "   A    rabbitmq.pnaltsw.site -> 142.93.248.100"
echo ""
read -p "¿Has configurado el DNS correctamente? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "⚠️  Por favor configura el DNS antes de continuar."
    echo "   Los certificados SSL no se generarán sin la configuración DNS correcta."
    exit 1
fi

# Crear directorio para certificados SSL
echo "🔐 Creando directorio para certificados SSL..."
mkdir -p letsencrypt
chmod 600 letsencrypt

# Detener servicios existentes si los hay
echo "🛑 Deteniendo servicios existentes..."
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml down

# Limpiar imágenes antiguas (opcional)
read -p "¿Quieres limpiar las imágenes Docker antiguas? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🧹 Limpiando imágenes antiguas..."
    docker system prune -f
fi

# Verificar que los puertos estén libres
echo "🔍 Verificando puertos..."
if netstat -tuln | grep -q ":80 "; then
    echo "⚠️  Puerto 80 está en uso. Detén el servicio que lo usa."
    netstat -tuln | grep ":80 "
fi

if netstat -tuln | grep -q ":443 "; then
    echo "⚠️  Puerto 443 está en uso. Detén el servicio que lo usa."
    netstat -tuln | grep ":443 "
fi

# Construir y levantar los servicios
echo "🔧 Construyendo y desplegando servicios con Traefik v3.6..."
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml up -d --build

# Esperar a que los servicios estén listos
echo "⏳ Esperando que los servicios estén listos..."
echo "   - Traefik generando certificados SSL..."
echo "   - Servicios de Spring Boot iniciando..."
echo "   - Bases de datos configurándose..."
sleep 45

# Verificar el estado de los servicios
echo "📊 Verificando estado de los servicios..."
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml ps

echo ""
echo "🎉 ¡Despliegue completado!"
echo ""
echo "🌐 URLs disponibles:"
echo "   • 🏠 Aplicación principal: https://pnaltsw.site"
echo "   • 🔌 API Gateway: https://api.pnaltsw.site"
echo "   • 📊 Grafana: https://grafana.pnaltsw.site (admin / GrafanaSecurePassword2024!)"
echo "   • 📈 Prometheus: https://prometheus.pnaltsw.site"
echo "   • 🐰 RabbitMQ: https://rabbitmq.pnaltsw.site (admin / RabbitMQSecurePassword2024!)"
echo "   • 🔀 Traefik Dashboard: https://traefik.pnaltsw.site (admin / [tu contraseña])"
echo ""
echo "🔐 Credenciales por defecto:"
echo "   • Traefik Dashboard: admin / admin (cambia en .env)"
echo "   • Grafana: admin / [tu password del .env]"
echo "   • RabbitMQ: admin / [tu password del .env]"
echo ""
echo "📝 Notas importantes:"
echo "   • Los certificados SSL se generan automáticamente con Let's Encrypt"
echo "   • Puede tardar 2-3 minutos en estar completamente operativo"
echo "   • Si hay errores de certificados, espera un poco más"
echo ""
echo "🔍 Comandos útiles:"
echo "   • Ver todos los logs: $DOCKER_COMPOSE_CMD -f docker-compose.prod.yml logs -f"
echo "   • Ver logs de Traefik: $DOCKER_COMPOSE_CMD -f docker-compose.prod.yml logs -f traefik"
echo "   • Ver certificados: docker exec traefik cat /letsencrypt/acme.json"
echo "   • Reiniciar todo: $DOCKER_COMPOSE_CMD -f docker-compose.prod.yml restart"
echo ""
echo "🛠️  Si tienes problemas:"
echo "   • Verifica que el DNS esté configurado correctamente"
echo "   • Asegúrate de que los puertos 80 y 443 estén abiertos"
echo "   • Revisa los logs con los comandos de arriba"
echo ""
echo "✅ Sistema desplegado exitosamente en https://pnaltsw.site"
