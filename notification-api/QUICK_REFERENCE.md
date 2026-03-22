# ⚡ QUICK REFERENCE - notification-api

Guia rápido de referência para trabalhar com a notification-api.

---

## 🚀 INICIAR

### Local
```bash
./gradlew bootRun
# http://localhost:8080/api/v1
```

### Docker
```bash
docker-compose up --build
```

### Test
```bash
./gradlew test
```

---

## 📡 API ENDPOINTS

### POST /api/v1/notifications

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sk_test_123456789" \
  -d '{
    "phone": "5511999999999",
    "message": "Olá!"
  }'
```

**Response (202):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING"
}
```

### GET /api/v1/actuator/health

**Request:**
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

---

## ⚙️ ENVIRONMENT VARIABLES

```env
NOTIFICATION_API_KEY=sk_prod_xyz              # Obrigatório
NOTIFICATION_SERVICE_URL=https://...          # Obrigatório
NOTIFICATION_TIMEOUT_MS=5000                  # Default: 5s
SERVER_PORT=8080                              # Default: 8080
```

---

## 🐛 VALIDAÇÕES

| Field | Validação | Erro |
|-------|-----------|------|
| `phone` | 10-15 dígitos | 400 VALIDATION_ERROR |
| `message` | 1-1000 caracteres | 400 VALIDATION_ERROR |
| `X-API-Key` | Exato match | 401 UNAUTHORIZED |

---

## 📋 ERROR CODES

| Code | Status | Descrição |
|------|--------|-----------|
| INVALID_PHONE | 400 | Telefone inválido |
| INVALID_MESSAGE | 400 | Mensagem inválida |
| VALIDATION_ERROR | 400 | Erro de validação |
| UNAUTHORIZED | 401 | API Key inválida |
| RATE_LIMIT_EXCEEDED | 429 | Limite excedido |
| TIMEOUT | 504 | Timeout ao chamar service |
| SERVICE_UNAVAILABLE | 503 | Service indisponível |
| INTERNAL_ERROR | 500 | Erro interno |

---

## 🔍 ESTRUTURA DO PROJETO

```
notification-api/
├── controller/       → HTTP endpoints
├── service/         → Lógica de orquestração
├── client/          → HTTP client (OpenFeign)
├── exception/       → Exceptions customizadas
├── filter/          → Interceptors (Correlation ID)
├── logger/          → Logs estruturados
├── context/         → ThreadLocal context
├── config/          → Configurações Spring
└── dto/             → Data transfer objects
```

---

## 📝 CÓDIGO COMUM

### Chamar outro serviço
```kotlin
val response = notificationServiceClient.send(
    request = SendNotificationRequest(...),
    apiKey = apiKey,
    correlationId = CorrelationIdContext.get()
)
```

### Log estruturado
```kotlin
logger.info(
    "evento_name",
    "campo1" to valor1,
    "campo2" to valor2
)
```

### Gerar erro
```kotlin
throw InvalidPhoneException("Formato inválido")
```

### Validar entrada
```kotlin
@field:NotBlank
@field:Pattern(regexp = "^\\d{10,15}$")
val phone: String
```

---

## 🧪 TESTES

### Executar
```bash
./gradlew test                    # Todos
./gradlew test --tests "*Controller*"  # Específico
```

### Estrutura
```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class NotificationApiApplicationTests {
    @Autowired private lateinit var mockMvc: MockMvc
    
    @Test
    fun `test name`() {
        mockMvc.post("/api/v1/notifications") {
            // ...
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
```

---

## 🐳 DOCKER

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

### Compose
```bash
docker-compose up --build
docker-compose down
```

---

## 🚀 DEPLOYMENT

### Railway
```bash
railway login
railway init
railway variables set NOTIFICATION_API_KEY=sk_prod_xyz
railway up
```

### Render
1. GitHub → Connect
2. New Web Service
3. Configure ENV vars
4. Deploy

### Fly.io
```bash
flyctl launch
flyctl secrets set NOTIFICATION_API_KEY=sk_prod_xyz
flyctl deploy
```

---

## 📚 DOCUMENTAÇÃO

| Arquivo | Leia quando... |
|---------|---|
| README.md | Quer overview geral |
| ARCHITECTURE.md | Quer entender design |
| DEPLOYMENT.md | Quer fazer deploy |
| CONTRIBUTING.md | Quer contribuir |

---

## 💡 TIPS

### Gerar UUID
```bash
# Linux/Mac
uuidgen

# Windows
[guid]::NewGuid()
```

### Testar API Key
```bash
# Gera chave segura
openssl rand -hex 32
```

### Ver logs
```bash
# Local
./gradlew bootRun | grep "correlationId"

# Railway
railway logs

# Docker
docker logs container-name
```

### Debug
```bash
# Spring debug logs
SPRING_PROFILES_ACTIVE=debug ./gradlew bootRun

# Feign debug
logging.level.feign=DEBUG
```

---

## 🔗 LINKS ÚTEIS

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [OpenFeign](https://cloud.spring.io/spring-cloud-openfeign/)
- [Railway Docs](https://docs.railway.app)
- [Render Docs](https://render.com/docs)

---

## 📞 TROUBLESHOOTING

**Q: Build falha?**
A: `./gradlew clean build`

**Q: Port 8080 em uso?**
A: `SERVER_PORT=8081 ./gradlew bootRun`

**Q: API Key inválida em produção?**
A: Atualize ENV var na plataforma

**Q: Timeout ao chamar service?**
A: Verifique NOTIFICATION_SERVICE_URL

**Q: Como ver logs estruturados?**
A: Procure por "correlationId" nos logs

---

**Pronto para produção! 🚀**
