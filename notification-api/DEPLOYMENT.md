# 🚀 DEPLOYMENT GUIDE - notification-api

Guia passo-a-passo para colocar a notification-api em produção **de forma totalmente gratuita**.

## 🎯 Escolha sua plataforma

| Plataforma | Custo | Facilidade | Recomendação |
|-----------|-------|-----------|--------------|
| **Railway** | Free (5 projetos) | ⭐⭐⭐⭐⭐ | ✅ **COMECE AQUI** |
| **Render** | Free (até 750h/mês) | ⭐⭐⭐⭐ | Boa alternativa |
| **Fly.io** | Free (3 shared-cpu-1x) | ⭐⭐⭐ | Mais técnico |

---

## 🚂 OPÇÃO 1: Railway (RECOMENDADO)

### ✅ Pré-requisitos
- Conta GitHub
- Node.js instalado (para o Railway CLI)
- Project já buildado localmente

### 📋 Passo a Passo

#### **Passo 1: Instalar Railway CLI**
```bash
npm install -g @railway/cli
```

#### **Passo 2: Fazer Login**
```bash
railway login
```
Vai abrir browser para autenticação via GitHub. Autorize.

#### **Passo 3: Criar Projeto**
```bash
cd notification-api
railway init
```
Responda:
- **Name**: `notification-api`
- **Select plugins**: Selecione "Dockerfile" (ou deixe vazio)

#### **Passo 4: Configurar Variáveis de Ambiente**
```bash
# Defina as variáveis de ambiente
railway variables set NOTIFICATION_API_KEY=sk_prod_seu_valor_seguro
railway variables set NOTIFICATION_SERVICE_URL=https://seu-notification-service.com
railway variables set NOTIFICATION_TIMEOUT_MS=5000
railway variables set SERVER_PORT=8080
```

#### **Passo 5: Deploy**
```bash
railway up
```

Railway vai:
1. Detectar o Dockerfile
2. Fazer build da imagem
3. Fazer deploy do container
4. Gerar URL pública

#### **Passo 6: Testar**
```bash
# Substitua YOUR_RAILWAY_URL pela URL gerada
curl https://YOUR_RAILWAY_URL/api/v1/actuator/health
```

Resposta esperada:
```json
{
  "status": "UP",
  "timestamp": 1703001234000,
  "service": "notification-api"
}
```

### 📊 Dashboard Railway

Acesse: https://railway.app/dashboard
- Logs em tempo real
- Métricas
- Variáveis de ambiente
- Redeploy fácil

### 🔄 Deploy Contínuo (CI/CD)

Para deploy automático quando você fazer push:

```bash
# Conectar GitHub
railway link

# Agora cada push dispara deploy automático
git push origin main
```

---

## 🎨 OPÇÃO 2: Render

### ✅ Pré-requisitos
- Conta GitHub
- Repository público no GitHub

### 📋 Passo a Passo

#### **Passo 1: Push para GitHub**
```bash
git remote add origin https://github.com/seu-usuario/notification-api.git
git branch -M main
git push -u origin main
```

#### **Passo 2: Conectar ao Render**
1. Acesse https://render.com
2. Clique em "New +"
3. Selecione "Web Service"
4. Conecte seu repositório GitHub

#### **Passo 3: Configurar**
- **Name**: `notification-api`
- **Runtime**: `Docker`
- **Build Command**: Deixe vazio (detecta Dockerfile automaticamente)
- **Start Command**: Deixe vazio

#### **Passo 4: Environment Variables**
Na aba "Environment":
```
NOTIFICATION_API_KEY=sk_prod_seu_valor
NOTIFICATION_SERVICE_URL=https://...
NOTIFICATION_TIMEOUT_MS=5000
SERVER_PORT=8080
```

#### **Passo 5: Deploy**
Clique em "Create Web Service"

Render vai fazer deploy automático. Acesse a URL gerada.

### 📊 Features Render
- ✅ Deploy automático via GitHub
- ✅ SSL/HTTPS automático
- ✅ Logs em tempo real
- ✅ Health check automático
- ✅ Restart automático se cair

---

## ✈️ OPÇÃO 3: Fly.io

### ✅ Pré-requisitos
- Conta Fly.io
- CLI do Fly instalado

### 📋 Passo a Passo

#### **Passo 1: Instalar CLI**
```bash
curl -L https://fly.io/install.sh | sh
```

#### **Passo 2: Fazer Login**
```bash
flyctl auth login
```

#### **Passo 3: Gerar Configuração**
```bash
cd notification-api
flyctl launch
```

Responda:
- **App name**: `notification-api` (ou deixe gerar)
- **Select Organization**: Use default
- **Select region**: Escolha mais próximo (e.g., `sjc` para São Francisco)
- **PostgreSQL**: Não
- **Redis**: Não
- **Save configuration?**: Yes

#### **Passo 4: Configurar Secrets**
```bash
flyctl secrets set \
  NOTIFICATION_API_KEY=sk_prod_seu_valor \
  NOTIFICATION_SERVICE_URL=https://... \
  NOTIFICATION_TIMEOUT_MS=5000
```

#### **Passo 5: Deploy**
```bash
flyctl deploy
```

#### **Passo 6: Testar**
```bash
flyctl open /api/v1/actuator/health
```

### 📊 Dashboard Fly
```bash
# Ver status
flyctl status

# Ver logs
flyctl logs

# Escalar
flyctl scale count 2
```

---

## 📋 CHECKLIST PÓS-DEPLOYMENT

- [ ] Health check retorna UP
- [ ] API Key está protegida (não commit em git)
- [ ] Logs estão sendo coletados
- [ ] HTTPS está habilitado
- [ ] Timeout está configurado
- [ ] Notification Service URL está apontando para versão correta

### Teste a API em Produção

```bash
# Health
curl https://seu-app.com/api/v1/actuator/health

# Enviar notificação
curl -X POST https://seu-app.com/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sk_prod_seu_valor" \
  -d '{
    "phone": "5511999999999",
    "message": "Teste em produção"
  }'
```

---

## 🔍 Troubleshooting

### ❌ Erro: "Build failed"
```bash
# Railway
railway logs  # Ver logs completos

# Render
# Veja na aba "Events" do dashboard

# Fly.io
flyctl logs
```

### ❌ API Key inválida em produção
```bash
# Atualize a variável
railway variables set NOTIFICATION_API_KEY=novo_valor

# Ou
flyctl secrets set NOTIFICATION_API_KEY=novo_valor

# Ou no Render, vá em Settings → Environment
```

### ❌ Timeout ao chamar notification-service
- Verifique se `NOTIFICATION_SERVICE_URL` está correto
- Verifique se notification-service está rodando
- Aumente `NOTIFICATION_TIMEOUT_MS` se necessário

### ❌ Health check falhando
```bash
# Acesse os logs
railway logs

# Verificar conexão
curl https://seu-app.com/api/v1/actuator/health -v
```

---

## 📚 Variáveis de Ambiente Finais

Para produção, configure EXATAMENTE:

```env
# OBRIGATÓRIO
NOTIFICATION_API_KEY=sk_prod_XXXX_YYYY_ZZZZ    # Gere uma chave segura!
NOTIFICATION_SERVICE_URL=https://seu-servico.com

# OPCIONAL
NOTIFICATION_TIMEOUT_MS=5000                     # Default: 5s
SERVER_PORT=8080                                 # Railway/Render detectam automaticamente
```

---

## 🔐 Segurança

### 🚨 IMPORTANTE: API Key

**NUNCA** commit sua API Key em git!

```bash
# ❌ ERRADO
echo "NOTIFICATION_API_KEY=sk_prod_123" >> .env
git add .env
git push

# ✅ CERTO
# Configure via dashboard da plataforma (Railway/Render/Fly)
# Ou em um arquivo .env.local que está no .gitignore
```

### Gerar API Key Segura

```bash
# Linux/Mac
openssl rand -hex 32

# Windows PowerShell
[System.Convert]::ToHexString((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

Use o valor gerado como `sk_prod_VALOR`.

---

## 🎯 Próximos Passos

1. ✅ Deploy em produção
2. ⬜ Configurar monitoramento (Datadog, New Relic)
3. ⬜ Implementar rate limiting
4. ⬜ Adicionar métricas Prometheus
5. ⬜ Configurar alertas de erro

---

## 💬 Dúvidas Frequentes

**P: Qual plataforma escolher?**
R: Railway é mais simples e grátis. Comece lá.

**P: Quanto custa?**
R: Railway: FREE (5 projetos pequenos)
   Render: FREE (750h/mês)
   Fly.io: FREE (3 shared-cpu-1x)

**P: Como atualizar após deployment?**
R: Se usar Railway/Render com GitHub, é automático. Só fazer push.

**P: Posso usar meu domínio personalizado?**
R: Sim! Configure CNAME nos DNS da sua plataforma.

---

## 📞 Suporte

- Railway Docs: https://docs.railway.app
- Render Docs: https://render.com/docs
- Fly.io Docs: https://fly.io/docs

**Sucesso no deployment! 🚀**
