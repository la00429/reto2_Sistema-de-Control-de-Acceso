#!/bin/bash

# Script de verificación e instalación para el VPS
echo "🔍 Verificando configuración del VPS..."

# Verificar Docker
echo "📦 Verificando Docker..."
if command -v docker &> /dev/null; then
    echo "✅ Docker está instalado: $(docker --version)"
    echo "🔄 Estado de Docker:"
    systemctl status docker --no-pager -l
else
    echo "❌ Docker no está instalado"
    exit 1
fi

# Verificar Docker Compose
echo ""
echo "🔧 Verificando Docker Compose..."
if docker compose version &> /dev/null; then
    echo "✅ Docker Compose (plugin) está disponible: $(docker compose version)"
elif command -v docker-compose &> /dev/null; then
    echo "✅ Docker Compose (standalone) está disponible: $(docker-compose --version)"
else
    echo "⚠️  Docker Compose no encontrado. Instalando..."
    # Instalar docker-compose standalone
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    echo "✅ Docker Compose instalado: $(docker-compose --version)"
fi

# Verificar firewall
echo ""
echo "🔥 Verificando firewall (UFW)..."
ufw status

echo ""
echo "📋 Configurando puertos necesarios..."
# Abrir puertos HTTP y HTTPS
ufw allow 80/tcp comment 'HTTP'
ufw allow 443/tcp comment 'HTTPS'

echo ""
echo "🔥 Estado final del firewall:"
ufw status

# Crear directorio de trabajo
echo ""
echo "📁 Preparando directorio de trabajo..."
mkdir -p /opt/access-control
cd /opt/access-control

echo ""
echo "📊 Información del sistema:"
echo "CPU: $(nproc) cores"
echo "RAM: $(free -h | awk '/^Mem:/ { print $2 }')"
echo "Disco: $(df -h / | awk 'NR==2 { print $4 " disponible de " $2 }')"
echo "IP: $(curl -s ifconfig.me)"

echo ""
echo "✅ VPS configurado y listo para el despliegue!"
echo ""
echo "🚀 Próximos pasos:"
echo "1. Clona tu repositorio en /opt/access-control"
echo "2. Configura el archivo .env"
echo "3. Ejecuta el script de despliegue"
