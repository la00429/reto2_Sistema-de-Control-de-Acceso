#!/bin/bash

# Script para generar hash de contraseña para Traefik Dashboard
echo "🔐 Generador de hash para Traefik Dashboard"
echo ""

# Verificar si htpasswd está instalado
if ! command -v htpasswd &> /dev/null; then
    echo "📦 Instalando apache2-utils para htpasswd..."
    sudo apt update
    sudo apt install -y apache2-utils
fi

# Solicitar contraseña
echo "Ingresa la contraseña para el dashboard de Traefik:"
read -s password

# Generar hash
hash=$(htpasswd -nB admin <<< "$password" | sed 's/\$/\$\$/g')

echo ""
echo "✅ Hash generado:"
echo "TRAEFIK_DASHBOARD_AUTH=$hash"
echo ""
echo "📝 Copia este valor en tu archivo .env:"
echo "TRAEFIK_DASHBOARD_AUTH=$hash"
