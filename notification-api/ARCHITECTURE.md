# 🏗️ ARCHITECTURE - notification-api

Documentação técnica da arquitetura da notification-api.

## 📊 Diagrama de Fluxo

```
┌─────────────┐
│   Client    │
│  (Postman)  │
└──────┬──────┘
       │ POST /api/v1/notifications
       │ Headers: X-API-Key, X-Correlation-ID
       ▼
┌──────────────────────────────────────┐
│      CorrelationIdFilter             │
│  - Gera ou propaga Correlation ID    │
│  - ThreadLocal para contexto         │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│   NotificationController             │
│  - Valida API Key (header)           │
│  - Valida @Valid DTO                 │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│   ValidationExceptionHandler         │
│  - 400 Bad Request se inválido       │
└──────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│   NotificationService                │
│  - Orquestra chamada ao client       │
│  - Trata erros específicos           │
│  - Log estruturado                   │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│   NotificationServiceClient          │
│  - OpenFeign HTTP client             │
│  - Adiciona headers (Correlation ID) │
│  - Timeout: 5s                       │
└──────────────┬───────────────────────┘
               │
               ▼ HTTP POST
       ┌───────────────────┐
       │ notification-     │
       │ service:8081      │
       └───────────────────┘
               │
               ▼
      ┌────────────────────┐
      │  Response 200 OK   │
      │  { id, status }    │
      └────────────┬───────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ NotificationResponse │
        │ id: UUID             │
        │ status: PENDING      │
        └──────────────┬───────┘
                       │
                       ▼
            ┌────────────────────┐
            │  202 ACCEPTED      │
            │  Response JSON     │
            └────────────────────┘
```

---

## 🧬 Estrutura de Componentes

### 🎮 Controller Layer

**File**: `controller/NotificationController.kt`

```kotlin
@RestController
@RequestMapping("/notifications")
class NotificationController {
    @PostMapping
    fun sendNotification(
        @Valid @RequestBody request: NotificationRequest,
        @RequestHeader("X-API-Key") apiKey: String
    ): ResponseEntity<NotificationResponse>
}
```

**Responsabilidades:**
- Validar API Key
- Aceitar DTO validado
- Orquestrar chamada ao service
- Retornar 202 ACCEPTED

**HTTP Status Codes:**
- 202 ACCEPTED: Sucesso
- 400 BAD REQUEST: Validação falhou
- 401 UNAUTHORIZED: API Key inválida

---

### 🧠 Service Layer

**File**: `service/NotificationService.kt`

```kotlin
@Service
class NotificationService(
    private val notificationServiceClient: NotificationServiceClient
) {
    fun sendNotification(request: NotificationRequest): NotificationResponse
}
```

**Responsabilidades:**
- Orquestra chamada ao notification-service
- Converte DTO de entrada para request do client
- Trata erros específicos (timeout, 503, etc)
- Logs estruturados para rastreamento

**Error Handling:**
- `SocketTimeoutException` → `TimeoutException` (504)
- `FeignException.Unauthorized` → `UnauthorizedException` (401)
- `FeignException.ServiceUnavailable` → `ServiceUnavailableException` (503)
- Qualquer outra exceção → `ServiceUnavailableException` (503)

---

### 🌉 Client Layer (OpenFeign)

**File**: `client/NotificationServiceClient.kt`

```kotlin
@FeignClient(
    name = "notification-service",
    url = "${notification.service.url}"
)
interface NotificationServiceClient {
    @PostMapping("/notifications")
    fun send(
        @RequestBody request: SendNotificationRequest,
        @RequestHeader("X-API-Key") apiKey: String,
        @RequestHeader("X-Correlation-ID") correlationId: String
    ): SendNotificationResponse
}
```

**Configuração** (application.yaml):
```yaml
feign:
  client:
    config:
      notification-service:
        connectTimeout: 5000      # 5 segundos
        readTimeout: 5000         # 5 segundos
        loggerLevel: basic
```

**Features:**
- Timeout configurável
- Suporta headers customizados
- Logs automáticos
- Tratamento de erros HTTP

---

### ✔️ Validação Layer

**File**: `dto/NotificationRequest.kt`

```kotlin
data class NotificationRequest(
    @field:NotBlank
    @field:Pattern(regexp = "^\\d{10,15}$")
    val phone: String,

    @field:NotBlank
    @field:Size(min = 1, max = 1000)
    val message: String
)
```

**Validações:**
- `@NotBlank`: Campo obrigatório
- `@Pattern`: Regex para formato de telefone
- `@Size`: Min/max characters para mensagem

**Erro gerado:**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "phone: Phone must be a valid number...",
  "timestamp": 1703001234000
}
```

---

### 🔍 Correlation ID

**Files**:
- `context/CorrelationIdContext.kt`: ThreadLocal holder
- `filter/CorrelationIdFilter.kt`: Intercepta requests

**Flow:**
```
1. Request chega com header X-Correlation-ID (ou vazio)
2. Filter intercepta e define no ThreadLocal
3. Service lê do ThreadLocal
4. Client adiciona ao header da requisição downstream
5. Response inclui correlationId implicitamente
6. Filter limpa ThreadLocal
```

**Uso:**
```kotlin
val correlationId = CorrelationIdContext.get()
// UUID automaticamente gerado se não existir
```

---

### 📝 Structured Logging

**File**: `logger/StructuredLogger.kt`

```kotlin
logger.info(
    "notification_request_received",
    "requestId" to requestId,
    "phone" to request.phone,
    "messageLength" to request.message.length
)
```

**Output JSON:**
```json
{
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "event": "notification_request_received",
  "requestId": "123e4567-e89b-12d3-a456-426614174000",
  "phone": "5511999999999",
  "messageLength": 15,
  "timestamp": 1703001234000
}
```

**Eventos Principais:**
- `notification_request_received`: Request chega no service
- `calling_notification_service`: Antes de chamar client
- `notification_sent_successfully`: Sucesso
- `notification_timeout`: Timeout
- `unauthorized_api_key`: API Key inválida
- `notification_service_unavailable`: Service down
- `notification_error`: Erro genérico

---

### ⚠️ Error Handling

**File**: `exception/GlobalExceptionHandler.kt`

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(...): ResponseEntity<ErrorResponse>
    
    @ExceptionHandler(NotificationException::class)
    fun handleNotificationException(...): ResponseEntity<ErrorResponse>
    
    @ExceptionHandler(Exception::class)
    fun handleGenericException(...): ResponseEntity<ErrorResponse>
}
```

**Erro Padrão:**
```json
{
  "code": "INVALID_PHONE",
  "message": "Phone must be a valid number with 10-15 digits",
  "timestamp": 1703001234000,
  "path": "/api/v1/notifications"
}
```

---

## 🔄 Request Lifecycle

```
1. REQUEST ARRIVES
   ├─ CorrelationIdFilter.doFilter()
   │  └─ Gera ou propaga X-Correlation-ID
   │
2. CONTROLLER
   ├─ NotificationController.sendNotification()
   │  ├─ Valida API Key
   │  ├─ @Valid DTO (validação automática)
   │  └─ Chama service
   │
3. SERVICE
   ├─ NotificationService.sendNotification()
   │  ├─ Log: notification_request_received
   │  ├─ Cria SendNotificationRequest
   │  ├─ Chama client
   │  └─ Log: notification_sent_successfully
   │
4. CLIENT
   ├─ NotificationServiceClient.send()
   │  ├─ FeignConfig.correlationIdInterceptor() adiciona header
   │  ├─ HTTP POST com timeout (5s)
   │  └─ Retorna SendNotificationResponse
   │
5. RESPONSE
   ├─ Converte para NotificationResponse
   ├─ HTTP 202 ACCEPTED
   └─ CorrelationIdFilter limpa ThreadLocal
```

---

## 📊 Data Flow

### Request

```json
POST /api/v1/notifications
Headers:
  X-API-Key: sk_prod_xyz
  X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000 (optional)

Body:
{
  "phone": "5511999999999",
  "message": "Olá!"
}
```

### Response (202)

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "status": "PENDING"
}
```

### Error Response (400/401/504)

```json
{
  "code": "INVALID_PHONE",
  "message": "Phone must be a valid number with 10-15 digits",
  "timestamp": 1703001234000,
  "path": "/api/v1/notifications"
}
```

---

## 🔐 Security Considerations

### API Key

- ✅ Validada em cada request
- ✅ Comparação em `NotificationController`
- ✅ Nunca logada em plaintext
- ✅ Configurada via ENV (não em código)

### Headers

- ✅ `X-API-Key`: Obrigatório, valor fixo
- ✅ `X-Correlation-ID`: Opcional, gerado automaticamente

### Input Validation

- ✅ Telefone: Regex `^\d{10,15}$`
- ✅ Mensagem: Size 1-1000 caracteres
- ✅ @NotBlank: Campos obrigatórios

---

## 🚀 Production Deployment

### Environment Variables

```env
# OBRIGATÓRIO
NOTIFICATION_API_KEY=sk_prod_XXXX

# OBRIGATÓRIO
NOTIFICATION_SERVICE_URL=https://notification-service.com

# OPCIONAL (defaults)
NOTIFICATION_TIMEOUT_MS=5000
SERVER_PORT=8080
```

### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jdk-jammy as builder
# Build stage

FROM eclipse-temurin:21-jdk-jammy
# Runtime: apenas JAR + dependencies
# Healthcheck: /api/v1/actuator/health
```

---

## 📈 Scalability

### Stateless Design

- ✅ Sem estado persistente
- ✅ Sem sessões
- ✅ ThreadLocal limpo após cada request
- ✅ Cada instância é independente

### Horizontal Scaling

```yaml
# docker-compose.yml
services:
  notification-api-1:
    ...
  notification-api-2:
    ...
  notification-api-3:
    ...
  # Load balancer na frente
```

### Performance

- Timeout: 5s (configurável)
- Connection pool: Padrão OpenFeign
- Compressão: Habilitada no Feign
- Logs: Estruturados mas sem overhead

---

## 🧪 Testing Strategy

### Unit Tests
- DTOs e validações
- CorrelationIdContext

### Integration Tests
- Controller endpoints
- GlobalExceptionHandler
- Mock do NotificationServiceClient

### End-to-End Tests (Manual)
- Postman collection incluída
- Testes de sucesso e erro

---

## 📚 Technologies Stack

| Camada | Tecnologia | Versão |
|--------|-----------|--------|
| **Language** | Kotlin | 2.2.21 |
| **Framework** | Spring Boot | 4.0.4 |
| **Web** | Spring MVC | - |
| **Validation** | Spring Validation | - |
| **HTTP Client** | OpenFeign | Spring Cloud 2025.1.1 |
| **JSON** | Jackson | - |
| **Logging** | SLF4J + Logback | - |
| **Testing** | JUnit 5 + MockMvc | - |
| **Build** | Gradle | - |
| **Java** | Temurin | 21 |

---

## 🔄 Future Enhancements

1. **Rate Limiting**
   - Bucket4j para limitar requests por API Key

2. **Caching**
   - Redis para cache de configurações

3. **Metrics**
   - Prometheus para observabilidade

4. **Tracing**
   - Jaeger para distributed tracing

5. **Circuit Breaker**
   - Resilience4j para falhas downstream

6. **Feature Flags**
   - Unleash para controle de features

---

## 📞 Contato & Suporte

Documentação: Ver `README.md`
Deployment: Ver `DEPLOYMENT.md`
API Docs: Ver Postman collection
