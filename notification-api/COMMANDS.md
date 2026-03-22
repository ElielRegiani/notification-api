# ⚡ ESSENTIAL COMMANDS - notification-api

Todos os comandos que você precisa usar.

---

## 🚀 QUICK START

```bash
# Build
./gradlew clean build

# Run
./gradlew bootRun

# Test
./gradlew test

# Docker
docker-compose up --build
```

---

## 🧪 TESTES

```bash
# Todos os testes
./gradlew test

# Testes específicos
./gradlew test --tests "*Controller*"

# Com cobertura
./gradlew test jacocoTestReport

# Health check
curl http://localhost:8080/api/v1/actuator/health
```

---

## 🚀 DESENVOLVIMENTO

```bash
# Build sem tests
./gradlew clean build -x test

# Run em modo debug
./gradlew bootRun --debug

# Clean
./gradlew clean

# Tasks disponíveis
./gradlew tasks
```

---

## 🐳 DOCKER

```bash
# Build image
docker build -t notification-api:latest .

# Run container
docker run -p 8080:8080 \
  -e NOTIFICATION_API_KEY=sk_prod_xyz \
  -e NOTIFICATION_SERVICE_URL=http://notification-service:8081 \
  notification-api:latest

# Docker Compose (dev)
docker-compose up --build

# Ver logs
docker logs <container-id>

# Stop
docker-compose down
```

---

## 📡 API TESTS

```bash
# Health check
curl http://localhost:8080/api/v1/actuator/health

# Send notification (sucesso)
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sk_test_123456789" \
  -d '{"phone": "5511999999999", "message": "Olá!"}'

# Invalid phone
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sk_test_123456789" \
  -d '{"phone": "123", "message": "Test"}'

# Missing API key
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{"phone": "5511999999999", "message": "Test"}'

# Com Correlation ID custom
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sk_test_123456789" \
  -H "X-Correlation-ID: my-custom-id-123" \
  -d '{"phone": "5511999999999", "message": "Test"}'
```

---

## 🔑 AMBIENTE

```bash
# Criar arquivo .env
cp .env.example .env

# Configurar variáveis
export NOTIFICATION_API_KEY=sk_prod_xyz
export NOTIFICATION_SERVICE_URL=https://...
export NOTIFICATION_TIMEOUT_MS=5000

# Ou local (Windows)
set NOTIFICATION_API_KEY=sk_prod_xyz
```

---

## 🚂 DEPLOYMENT (RAILWAY)

```bash
# Instalar Railway CLI
npm install -g @railway/cli

# Login
railway login

# Inicializar projeto
railway init

# Configurar variáveis
railway variables set NOTIFICATION_API_KEY=sk_prod_xyz
railway variables set NOTIFICATION_SERVICE_URL=https://...
railway variables set NOTIFICATION_TIMEOUT_MS=5000

# Deploy
railway up

# Ver logs
railway logs

# Status
railway status

# Abrir app
railway open
```

---

## 🎨 GIT COMMANDS

```bash
# Clone
git clone https://github.com/seu-usuario/notification-api.git
cd notification-api

# Branch feature
git checkout -b feature/nova-feature

# Commit
git commit -m "feat: nova feature"

# Push
git push origin feature/nova-feature

# Pull request
# Abra no GitHub web
```

---

## 📊 BUILD INFO

```bash
# Ver gradle version
./gradlew --version

# Clean build
./gradlew clean build

# Build com offline mode
./gradlew build --offline

# Ver dependências
./gradlew dependencies

# Ver task graph
./gradlew build --dry-run
```

---

## 🔧 TROUBLESHOOTING

```bash
# Build falha
./gradlew clean build

# Port 8080 em uso
lsof -i :8080                    # Mac/Linux
netstat -ano | findstr :8080     # Windows

# Usar porta diferente
SERVER_PORT=8081 ./gradlew bootRun

# Limpar cache gradle
./gradlew --stop

# Reset gradle
rm -rf ~/.gradle/caches
./gradlew clean build

# Ver erros de compilação
./gradlew compileKotlin
```

---

## 📝 LOGS

```bash
# Local (human readable)
./gradlew bootRun | grep "correlationId"

# Com DEBUG
./gradlew bootRun -i DEBUG

# Docker logs
docker logs -f <container-id>

# Railway logs
railway logs -f
```

---

## 🔑 GERAR CHAVES

```bash
# Linux/Mac: Gerar API key
openssl rand -hex 32

# Windows PowerShell: Gerar API key
[System.Convert]::ToHexString((1..32 | ForEach-Object { Get-Random -Maximum 256 }))

# UUID (para testing)
# Linux/Mac
uuidgen

# Windows PowerShell
[guid]::NewGuid()
```

---

## 📦 POSTMAN

```bash
# Importar collection
# Abra Postman → Import → Raw JSON
# Cole o conteúdo de postman_collection.json

# Ou importe o arquivo direto
# File → Import → Select postman_collection.json

# Variáveis no Postman
{{base_url}} = http://localhost:8080/api/v1
{{api_key}} = sk_test_123456789
```

---

## 📂 ARQUIVO STRUCTURE

```bash
# Ver estrutura
tree -L 3 -I ".gradle|build|.git"

# Ou listar
find . -type f -name "*.kt" | head -20
find . -type f -name "*.yaml"
find . -type f -name "*.md"
```

---

## 🔐 SECRETS

```bash
# NÃO COMMITAR NUNCA
# .env
# .env.local
# *.pem
# *.key

# Usar .env.example como referência
cp .env.example .env

# Adicionar .env ao .gitignore
echo ".env" >> .gitignore
```

---

## 🚀 CHECKLISTS RÁPIDOS

### Antes de commitar
```bash
./gradlew clean build
./gradlew test
git status
```

### Antes de deploy
```bash
./gradlew clean build
docker build -t notification-api:latest .
# Testar Docker localmente
docker run -p 8080:8080 ... notification-api:latest
```

### Depois de deploy
```bash
# Testar health
curl https://seu-app.com/api/v1/actuator/health

# Testar API
curl -X POST https://seu-app.com/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sk_prod_xyz" \
  -d '{"phone": "5511999999999", "message": "Test"}'

# Ver logs
railway logs (ou dashboard)
```

---

## 📊 PERFORMANCE

```bash
# Medir tempo de build
time ./gradlew clean build

# Medir tempo de test
./gradlew test --profile

# Ver memory usage
./gradlew build --scan
```

---

## 🧹 LIMPEZA

```bash
# Limpar build
./gradlew clean

# Limpar gradle cache
rm -rf ~/.gradle/caches

# Limpar docker images
docker image prune

# Limpar docker containers
docker container prune

# Full cleanup (cuidado!)
./gradlew clean
rm -rf build/
docker system prune
```

---

## 📚 HELP

```bash
# Gradle help
./gradlew help
./gradlew tasks

# Spring Boot help
./gradlew bootRun --help

# Kotlin help
./gradlew compileKotlin --help
```

---

**Bookmark esses comandos! 📚**
