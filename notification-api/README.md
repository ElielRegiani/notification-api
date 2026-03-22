# notification-api 🚀

Um serviço de API leve para orquestrações de notificações via WhatsApp. Simples por design, bem feito por engenharia.

## 📋 Responsabilidades

✅ Expor API HTTP POST `/notifications`
✅ Validar entrada (telefone, mensagem)
✅ Autenticação via API Key
✅ Orquestrações leves (chama notification-service)
✅ Logs estruturados com Correlation ID
✅ Tratamento de erros e resiliência

❌ NÃO faz:
- Lógica de envio real
- Integração com WhatsApp
- Processamento assíncrono
- Regras de negócio complexas

## 🏗️ Arquitetura

```
Controller → Service → NotificationServiceClient
     ↓
  Logs Estruturados + Correlation ID
```

## 🚀 Quick Start

### Local (Development)

```bash
# Clone o repositório
git clone ...
cd notification-api

# Configure as variáveis de ambiente
cp .env.example .env
# Edite .env com suas configurações

# Build
./gradlew clean build

# Run
./gradlew bootRun
```

### Docker Compose

```bash
docker-compose up --build
```

A API estará disponível em: `http://localhost:8080/api/v1`

## 📡 API Endpoints

### POST /api/v1/notifications

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sk_test_123456789" \
  -d '{
    "phone": "5511999999999",
    "message": "Olá, tudo bem?"
  }'
```

**Response (202 Accepted):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING"
}
```

**Validações:**
- `phone`: Obrigatório, 10-15 dígitos (e.g., 5511999999999)
- `message`: Obrigatório, 1-1000 caracteres
- `X-API-Key`: Obrigatório no header

### GET /api/v1/actuator/health

Health check simples:
```bash
curl http://localhost:8080/api/v1/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": 1703001234000,
  "service": "notification-api"
}
```

## 🛡️ Error Handling

Todos os erros seguem um padrão:

```json
{
  "code": "INVALID_PHONE",
  "message": "Phone must be a valid number with 10-15 digits",
  "timestamp": 1703001234000,
  "path": "/api/v1/notifications"
}
```

### Códigos de Erro

| Code | Status | Descrição |
|------|--------|-----------|
| `INVALID_PHONE` | 400 | Telefone inválido |
| `INVALID_MESSAGE` | 400 | Mensagem inválida ou vazia |
| `VALIDATION_ERROR` | 400 | Erro de validação genérico |
| `UNAUTHORIZED` | 401 | API Key inválida |
| `RATE_LIMIT_EXCEEDED` | 429 | Limite de requisições excedido |
| `TIMEOUT` | 504 | Timeout ao chamar notification-service |
| `SERVICE_UNAVAILABLE` | 503 | notification-service indisponível |
| `INTERNAL_ERROR` | 500 | Erro interno do servidor |

## 🔍 Logs Estruturados

Todos os logs incluem:
- `correlationId`: ID único para rastreamento de requisição
- `event`: Nome do evento
- `timestamp`: Timestamp do evento

**Exemplo:**
```json
{
  "correlationId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "event": "notification_request_received",
  "phone": "5511999999999",
  "messageLength": 15,
  "timestamp": 1703001234000
}
```

Você pode passar um Correlation ID customizado no header:
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "X-Correlation-ID: my-custom-id-123"
```

## 🐳 Docker

### Build

```bash
docker build -t notification-api:latest .
```

### Run

```bash
docker run -p 8080:8080 \
  -e NOTIFICATION_API_KEY=sk_prod_xyz \
  -e NOTIFICATION_SERVICE_URL=http://notification-service:8081 \
  notification-api:latest
```

## 🚀 Deploy em Produção

### 1. Railway (Recomendado - Mais simples)

```bash
# Instale o CLI
npm install -g @railway/cli

# Faça login
railway login

# Crie um novo projeto
railway init

# Configure as variáveis de ambiente
railway variables set NOTIFICATION_API_KEY=sk_prod_xyz
railway variables set NOTIFICATION_SERVICE_URL=https://notification-service.seu-dominio.com

# Deploy
railway up
```

### 2. Render

1. Conecte seu repositório GitHub no Render
2. Crie um novo "Web Service"
3. Configure:
   - **Runtime**: Docker
   - **Build Command**: Automático (lê Dockerfile)
   - **Start Command**: Automático

4. Em "Environment", adicione:
   - `NOTIFICATION_API_KEY`
   - `NOTIFICATION_SERVICE_URL`
   - `NOTIFICATION_TIMEOUT_MS`

### 3. Fly.io

```bash
# Instale o CLI
curl -L https://fly.io/install.sh | sh

# Faça login
flyctl auth login

# Crie um app
flyctl launch

# Configure secrets
flyctl secrets set NOTIFICATION_API_KEY=sk_prod_xyz

# Deploy
flyctl deploy
```

## 📊 Monitoramento & Observabilidade

### Health Endpoint

```bash
curl https://seu-app.railway.app/api/v1/actuator/health
```

### Logs

Acesse os logs estruturados via:

**Railway:**
```bash
railway logs
```

**Render/Fly.io:** Dashboard → Logs

### Métricas Importantes

- **Latência**: Monitore a latência de requisições
- **Taxa de Erro**: Erros 4xx vs 5xx
- **Timeout**: Quantas requisições fizeram timeout
- **Correlation ID**: Rastrie requisições end-to-end

## 🔥 Diferenciais Sênior (TODO - Próximas Fases)

- [ ] Rate limiting (OAuth2 Proxy ou middleware customizado)
- [ ] Timeout configurável por endpoint
- [ ] Feature flags (Unleash, LaunchDarkly)
- [ ] OpenAPI/Swagger generator
- [ ] Métricas Prometheus
- [ ] Tracing distribuído (Jaeger)
- [ ] Cache inteligente
- [ ] Circuit breaker (Resilience4j)

## 🧪 Testing

```bash
# Run tests
./gradlew test

# Run tests com cobertura
./gradlew test jacocoTestReport
```

## 📝 Exemplos de Uso

### Postman

Importe a coleção Postman:
```
File → Import → Raw JSON
```

[TODO: adicionar JSON da coleção]

### cURL

```bash
# Request bem-sucedido
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sk_test_123456789" \
  -d '{"phone": "5511999999999", "message": "Teste"}'

# Erro: API Key inválida
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "X-API-Key: invalid_key" \
  -d '{"phone": "5511999999999", "message": "Teste"}'

# Erro: Telefone inválido
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sk_test_123456789" \
  -d '{"phone": "123", "message": "Teste"}'
```

## 🤝 Contribuindo

1. Crie uma branch (`git checkout -b feature/XYZ`)
2. Commit suas mudanças (`git commit -m 'Add XYZ'`)
3. Push para a branch (`git push origin feature/XYZ`)
4. Abra um Pull Request

## 📜 License

MIT

## 📞 Suporte

Para dúvidas ou issues, abra uma issue no repositório.

---

**Feito com ❤️ para produção.**
