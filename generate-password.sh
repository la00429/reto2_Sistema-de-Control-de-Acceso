#!/bin/bash

# Script para generar hash de contraseña para Traefik Dashboard
echo "🔐 Generador de hash para Traefik Dashboard"
echo ""

# Verificar si htpasswd está instalado
if ! command -v htpasswd &> /dev/null; then
    echo "⚠️  htpasswd no está instalado. Instalándolo..."
    if command -v apt-get &> /dev/null; then
        sudo apt-get update && sudo apt-get install -y apache2-utils
    elif command -v yum &> /dev/null; then
        sudo yum install -y httpd-tools
    else
        echo "❌ No se pudo instalar htpasswd automáticamente."
        echo "   Por favor instala apache2-utils (Debian/Ubuntu) o httpd-tools (RHEL/CentOS)"
        exit 1
    fi
fi

# Solicitar credenciales
read -p "Usuario para el dashboard (default: admin): " USERNAME
USERNAME=${USERNAME:-admin}

read -s -p "Contraseña para el dashboard: " PASSWORD
echo ""

if [ -z "$PASSWORD" ]; then
    echo "❌ La contraseña no puede estar vacía"
    exit 1
fi

# Generar hash
echo ""
echo "🔄 Generando hash..."
HASH=$(echo $(htpasswd -nB $USERNAME $PASSWORD) | sed -e 's/\$/\$\$/g')

echo ""
echo "✅ Hash generado exitosamente:"
echo ""
echo "Agrega estas líneas a tu archivo .env:"
echo "TRAEFIK_DASHBOARD_USER=$USERNAME"
echo "TRAEFIK_DASHBOARD_PASSWORD_HASH=$HASH"
echo ""
echo "O copia directamente este valor para TRAEFIK_DASHBOARD_PASSWORD_HASH:"
echo "$HASH"
echo ""
