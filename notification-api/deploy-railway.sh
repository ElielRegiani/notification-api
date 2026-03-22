#!/bin/bash

# ========================================
# Deploy script para Railway
# ========================================
# Uso: ./deploy-railway.sh
# Pré-requisitos: railway CLI instalado
# ========================================

set -e

echo "🚀 Iniciando deploy no Railway..."

# Validar se railway está instalado
if ! command -v railway &> /dev/null; then
    echo "❌ Railway CLI não está instalado!"
    echo "Instale com: npm install -g @railway/cli"
    exit 1
fi

# Validar se existe .env
if [ ! -f .env ]; then
    echo "⚠️  Arquivo .env não encontrado!"
    echo "Copie .env.example para .env e configure as variáveis"
    exit 1
fi

# Fazer login se necessário
echo "🔐 Verificando autenticação..."
railway token > /dev/null 2>&1 || railway login

# Build local
echo "🔨 Buildando projeto..."
./gradlew clean build -x test

# Deploy
echo "📦 Enviando para Railway..."
railway up

echo "✅ Deploy concluído!"
echo "Acesse sua aplicação em: https://seu-app.railway.app/api/v1/actuator/health"
