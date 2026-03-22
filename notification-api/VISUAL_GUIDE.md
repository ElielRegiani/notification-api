# 🎨 VISUAL GUIDE - notification-api

Guia visual da arquitetura e fluxos.

---

## 📊 REQUEST FLOW

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT (Postman/cURL)                        │
│  POST /api/v1/notifications                                         │
│  Headers: X-API-Key, X-Correlation-ID (optional)                   │
│  Body: { "phone": "55...", "message": "..." }                      │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   CorrelationIdFilter                              │
│  • Intercepta todos os requests                                     │
│  • Lê X-Correlation-ID do header (ou gera UUID)                    │
│  • Armazena em ThreadLocal                                          │
│  • Propaga para downstream                                          │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│              NotificationController.sendNotification()              │
│  @PostMapping("/notifications")                                     │
│  • Valida X-API-Key header (401 se inválido)                       │
│  • @Valid NotificationRequest (400 se falhar)                      │
│  • Log: notification_request_started                                │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼ (API Key ✅ + Request ✅)
┌─────────────────────────────────────────────────────────────────────┐
│              NotificationService.sendNotification()                 │
│  • Log: notification_request_received                               │
│  • Cria SendNotificationRequest                                     │
│  • Chama NotificationServiceClient.send()                           │
│  • Trata erros específicos                                          │
│  • Log: notification_sent_successfully ou erro                      │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│             NotificationServiceClient (OpenFeign)                   │
│  @PostMapping("/notifications")                                     │
│  • FeignConfig interceptor adiciona Correlation-ID                 │
│  • HTTP POST com timeout (5s)                                       │
│  • Conecta em: NOTIFICATION_SERVICE_URL                             │
└────────────────────────────┬────────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
    SUCCESS             TIMEOUT              ERROR
   (200-299)          (sock timeout)        (4xx, 5xx)
        │                   │                   │
        │ Response ✅        │ Erro 504         │ Erro 503/401
        │                   │                   │
        └────────────────────┼───────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│            GlobalExceptionHandler (se necessário)                   │
│  • Mapeia NotificationException → ErrorResponse                     │
│  • Mapeia ValidationException → ErrorResponse                       │
│  • Status code correto (400/401/503/504)                           │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Response (HTTP)                                │
│                                                                      │
│  202 ACCEPTED (sucesso)                  4xx/5xx (erro)             │
│  {                                       {                          │
│    "id": "uuid",                           "code": "ERROR_CODE",   │
│    "status": "PENDING"                     "message": "...",       │
│  }                                         "timestamp": 123456,     │
│                                            "path": "/api/v1/..."   │
│                                          }                          │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│           CorrelationIdFilter (cleanup)                            │
│  • Remove ThreadLocal (previne memory leak)                         │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │  CLIENT RECEBE  │
                    │  RESPOSTA ✅    │
                    └─────────────────┘
```

---

## 🏗️ COMPONENT ARCHITECTURE

```
┌────────────────────────────────────────────────────────────────────┐
│                         NOTIFICATION-API                            │
│                    (Spring Boot Application)                        │
├────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ HTTP LAYER                                                   │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │  • Tomcat Server (port 8080)                                │  │
│  │  • Context path: /api/v1                                    │  │
│  │  • CorrelationIdFilter (interceptor)                        │  │
│  └──────────────────┬───────────────────────────────────────────┘  │
│                     │                                               │
│  ┌──────────────────▼───────────────────────────────────────────┐  │
│  │ CONTROLLER LAYER                                             │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │  NotificationController                                      │  │
│  │  ├─ POST /notifications                                     │  │
│  │  │  └─ Valida API Key                                       │  │
│  │  │  └─ @Valid DTO                                           │  │
│  │  └─ GlobalExceptionHandler                                  │  │
│  │                                                              │  │
│  │  HealthController                                            │  │
│  │  └─ GET /actuator/health                                    │  │
│  └──────────────────┬───────────────────────────────────────────┘  │
│                     │                                               │
│  ┌──────────────────▼───────────────────────────────────────────┐  │
│  │ SERVICE LAYER                                                │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │  NotificationService                                         │  │
│  │  ├─ Orquestra chamadas                                       │  │
│  │  ├─ Trata erros                                              │  │
│  │  └─ Logs estruturados                                        │  │
│  └──────────────────┬───────────────────────────────────────────┘  │
│                     │                                               │
│  ┌──────────────────▼───────────────────────────────────────────┐  │
│  │ CLIENT LAYER (OpenFeign)                                     │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │  NotificationServiceClient                                   │  │
│  │  ├─ @FeignClient                                            │  │
│  │  ├─ Timeout: 5s (configurável)                              │  │
│  │  └─ FeignConfig (interceptor)                               │  │
│  │     └─ Adiciona Correlation-ID                              │  │
│  └──────────────────┬───────────────────────────────────────────┘  │
│                     │                                               │
│  ┌──────────────────▼───────────────────────────────────────────┐  │
│  │ DTO LAYER                                                    │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │  NotificationRequest                                         │  │
│  │  ├─ @NotBlank phone                                          │  │
│  │  ├─ @Pattern (regex)                                         │  │
│  │  └─ @Size message                                            │  │
│  │                                                              │  │
│  │  NotificationResponse                                        │  │
│  │  ├─ id (UUID)                                               │  │
│  │  ├─ status (PENDING)                                         │  │
│  │                                                              │  │
│  │  ErrorResponse                                               │  │
│  │  ├─ code                                                     │  │
│  │  ├─ message                                                  │  │
│  │  └─ timestamp                                                │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ CROSS-CUTTING CONCERNS                                       │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │  CorrelationIdContext                                        │  │
│  │  ├─ ThreadLocal<String>                                      │  │
│  │  └─ Propaga correlationId                                    │  │
│  │                                                              │  │
│  │  StructuredLogger                                            │  │
│  │  ├─ JSON output                                              │  │
│  │  └─ Inclui correlationId                                     │  │
│  │                                                              │  │
│  │  GlobalExceptionHandler                                      │  │
│  │  ├─ @RestControllerAdvice                                    │  │
│  │  └─ Mapeia exceptions → ErrorResponse                        │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │ CONFIGURATION                                                │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │  application.yaml                                            │  │
│  │  ├─ Server config                                            │  │
│  │  ├─ Feign timeouts                                           │  │
│  │  ├─ Logging levels                                           │  │
│  │  └─ ENV vars support                                         │  │
│  │                                                              │  │
│  │  FeignConfig                                                 │  │
│  │  └─ RequestInterceptor (@Bean)                              │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
└────────────────────────────────────────────────────────────────────┘
         │
         └──► HTTP POST
              └──► notification-service:8081
```

---

## 🔄 ERROR HANDLING FLOW

```
              REQUEST
                │
                ▼
          ┌──────────────┐
          │ VALIDATION?  │
          └──┬────────┬──┘
         NO │        │ YES
            │        ▼
            │   ┌─────────────────────┐
            │   │ @Valid DTO fails?   │
            │   └─────────┬───────────┘
            │             │ YES
            │             ▼
            │   ┌─────────────────────────────────────┐
            │   │ MethodArgumentNotValidException     │
            │   │           ▼                         │
            │   │ GlobalExceptionHandler              │
            │   │           ▼                         │
            │   │ 400 BAD REQUEST                     │
            │   │ ErrorResponse {                     │
            │   │   "code": "VALIDATION_ERROR",      │
            │   │   "message": "field errors"         │
            │   │ }                                   │
            │   └─────────────────────────────────────┘
            │
            ▼
        ┌──────────────┐
        │ API KEY OK?  │
        └──┬────────┬──┘
       YES │        │ NO
           │        ▼
           │   ┌─────────────────────────────────────┐
           │   │ UnauthorizedException               │
           │   │           ▼                         │
           │   │ GlobalExceptionHandler              │
           │   │           ▼                         │
           │   │ 401 UNAUTHORIZED                    │
           │   │ ErrorResponse {                     │
           │   │   "code": "UNAUTHORIZED"            │
           │   │ }                                   │
           │   └─────────────────────────────────────┘
           │
           ▼
        ┌──────────────────┐
        │ CALL SERVICE OK? │
        └──┬────────┬──────┘
       YES │        │ NO
           │        ▼
           │   ┌─────────────────────────────────────┐
           │   │ Qual erro?                          │
           │   ├────────────┬────────────┬───────────┤
           │   │            │            │           │
           │   ▼            ▼            ▼           ▼
           │ TIMEOUT     503         401        GENERIC
           │   │          │          │           │
           │   ▼          ▼          ▼           ▼
           │ 504      503           401         503
           │ TIMEOUT  SERVICE_      UNAUTHORIZED SERVICE_
           │          UNAVAILABLE                UNAVAILABLE
           │   │          │          │           │
           │   └──────────┴──────────┴───────────┘
           │                │
           │                ▼
           │   ┌─────────────────────────────────────┐
           │   │ GlobalExceptionHandler              │
           │   │           ▼                         │
           │   │ ErrorResponse {                     │
           │   │   "code": "...",                   │
           │   │   "message": "...",                 │
           │   │   "timestamp": 123456,              │
           │   │   "path": "/api/v1/notifications"  │
           │   │ }                                   │
           │   └─────────────────────────────────────┘
           │
           ▼
        ┌──────────────────┐
        │ SUCCESS (200)    │
        └──────────┬───────┘
                   │
                   ▼
        ┌──────────────────────────────────────┐
        │ 202 ACCEPTED                         │
        │ NotificationResponse {               │
        │   "id": "550e8400-...",             │
        │   "status": "PENDING"                │
        │ }                                    │
        └──────────────────────────────────────┘
```

---

## 🚀 DEPLOYMENT ARCHITECTURE

```
┌──────────────────────────────────────────────────────────────────────┐
│                        PRODUCTION DEPLOYMENT                         │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │ RAILWAY / RENDER / FLY.IO                                      │  │
│  ├────────────────────────────────────────────────────────────────┤  │
│  │                                                                 │  │
│  │  ┌──────────────────────────────────────────────────────────┐  │  │
│  │  │ Load Balancer / Edge Network                            │  │  │
│  │  │ • SSL/TLS termination                                   │  │  │
│  │  │ • Auto HTTPS                                            │  │  │
│  │  └──────────────┬───────────────────────────────────────────┘  │  │
│  │                 │                                               │  │
│  │  ┌──────────────▼───────────────────────────────────────────┐  │  │
│  │  │ Container Registry                                       │  │  │
│  │  │ • Dockerfile build                                       │  │  │
│  │  │ • Multi-stage optimization                              │  │  │
│  │  │ • ~300MB image                                          │  │  │
│  │  └──────────────┬───────────────────────────────────────────┘  │  │
│  │                 │                                               │  │
│  │  ┌──────────────▼───────────────────────────────────────────┐  │  │
│  │  │ Instances (1-N)                                          │  │  │
│  │  │ • notification-api-1                                     │  │  │
│  │  │ • notification-api-2                                     │  │  │
│  │  │ • notification-api-3                                     │  │  │
│  │  │                                                          │  │  │
│  │  │ ┌────────────────────────────────────────────────────┐  │  │  │
│  │  │ │ Instance Configuration                            │  │  │  │
│  │  │ ├────────────────────────────────────────────────────┤  │  │  │
│  │  │ │ Environment Variables:                            │  │  │  │
│  │  │ │ • NOTIFICATION_API_KEY=sk_prod_xyz               │  │  │  │
│  │  │ │ • NOTIFICATION_SERVICE_URL=https://...           │  │  │  │
│  │  │ │ • NOTIFICATION_TIMEOUT_MS=5000                   │  │  │  │
│  │  │ │ • SERVER_PORT=8080                               │  │  │  │
│  │  │ │                                                   │  │  │  │
│  │  │ │ Health Check:                                     │  │  │  │
│  │  │ │ • /api/v1/actuator/health                         │  │  │  │
│  │  │ │ • Interval: 30s                                   │  │  │  │
│  │  │ │ • Timeout: 5s                                     │  │  │  │
│  │  │ │ • Retries: 3                                      │  │  │  │
│  │  │ │                                                   │  │  │  │
│  │  │ │ Logs:                                             │  │  │  │
│  │  │ │ • Structured JSON logs                            │  │  │  │
│  │  │ │ • Dashboard integration                           │  │  │  │
│  │  │ └────────────────────────────────────────────────────┘  │  │  │
│  │  │                                                          │  │  │
│  │  └──────────────┬───────────────────────────────────────────┘  │  │
│  │                 │                                               │  │
│  │  ┌──────────────▼───────────────────────────────────────────┐  │  │
│  │  │ Outbound Connection                                      │  │  │
│  │  │ • API → notification-service                             │  │  │
│  │  │ • Timeout: 5s                                            │  │  │
│  │  │ • Correlation ID propagation                             │  │  │
│  │  └──────────────────────────────────────────────────────────┘  │  │
│  │                                                                 │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │ MONITORING (Dashboard)                                         │  │
│  ├────────────────────────────────────────────────────────────────┤  │
│  │ • Instance logs                                                │  │
│  │ • Metrics (CPU, Memory, Network)                              │  │
│  │ • Deploy history                                               │  │
│  │ • Auto-restart on crash                                        │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 📈 SCALING STRATEGY

```
LOW TRAFFIC                    MEDIUM TRAFFIC              HIGH TRAFFIC
(<100 req/s)                   (100-1k req/s)              (>1k req/s)

┌──────────┐                  ┌──────────┐                 ┌──────────┐
│Instance 1│                  │Instance 1│                 │Instance 1│
│8GB RAM   │                  │8GB RAM   │                 │8GB RAM   │
│2 CPU     │                  │2 CPU     │                 │2 CPU     │
│PORT:8080 │                  │PORT:8080 │                 │PORT:8080 │
└──────────┘                  ├──────────┤                 ├──────────┤
    │                         │          │                 │          │
    │                         │Instance 2│                 │Instance 2│
    │                         │8GB RAM   │                 │8GB RAM   │
    │                         │2 CPU     │                 │2 CPU     │
    │                         │PORT:8080 │                 │PORT:8080 │
    │                         └──────────┘                 ├──────────┤
    │                             │                        │          │
    └─────────────────────────────┼────────────────────────┼──────────┘
                                  │                        │
                      ┌───────────┴────────────┐  ┌────────┴──────────┐
                      ▼                        ▼  ▼                   ▼
                  LOAD BALANCER            LOAD BALANCER       LOAD BALANCER
                  (Railway/Render)         (Railway/Render)    (Railway/Render)
                      │                        │                   │
                      ▼                        ▼                   ▼
                   CLIENTS                  CLIENTS            CLIENTS

Cost: $0-5/mô    Cost: $5-20/mô            Cost: $20-50/mô
```

---

## 🔐 SECURITY LAYERS

```
┌────────────────────────────────────────────────────────┐
│                   INGRESS REQUEST                      │
└─────────────────────┬──────────────────────────────────┘
                      │
                      ▼
┌────────────────────────────────────────────────────────┐
│ Layer 1: Network Security (Platform)                   │
│ • SSL/TLS encryption                                   │
│ • HTTPS only                                           │
│ • DDoS protection                                      │
└─────────────────────┬──────────────────────────────────┘
                      │
                      ▼
┌────────────────────────────────────────────────────────┐
│ Layer 2: Authentication (API)                          │
│ • X-API-Key validation                                 │
│ • 401 if invalid                                       │
└─────────────────────┬──────────────────────────────────┘
                      │ ✅ API Key OK
                      ▼
┌────────────────────────────────────────────────────────┐
│ Layer 3: Input Validation (DTO)                        │
│ • Phone: regex 10-15 digits                            │
│ • Message: size 1-1000                                 │
│ • 400 if invalid                                       │
└─────────────────────┬──────────────────────────────────┘
                      │ ✅ Input valid
                      ▼
┌────────────────────────────────────────────────────────┐
│ Layer 4: Output Protection (Exception Handler)        │
│ • No stack traces                                      │
│ • Generic error messages                              │
│ • No sensitive data exposure                          │
└─────────────────────┬──────────────────────────────────┘
                      │ ✅ Response safe
                      ▼
┌────────────────────────────────────────────────────────┐
│ Layer 5: Audit & Logging (Correlation ID)             │
│ • All actions logged                                   │
│ • Correlation ID for tracing                          │
│ • No secrets in logs                                  │
└─────────────────────┬──────────────────────────────────┘
                      │
                      ▼
                 ✅ RESPONSE OK
```

---

## 📊 DATA TYPES FLOW

```
┌────────────────────────────┐
│  NotificationRequest (Input)│
├────────────────────────────┤
│ phone: String              │
│ message: String            │
└────────┬───────────────────┘
         │
         ▼
    ┌────────────────────────────────┐
    │ Validação Automática (@Valid) │
    └────────┬───────────────────────┘
             │
             ▼
    ┌──────────────────────────────────┐
    │ SendNotificationRequest (Client) │
    ├──────────────────────────────────┤
    │ phone: String                    │
    │ message: String                  │
    │ correlationId: String            │
    └────────┬───────────────────────────┘
             │
             ▼ HTTP POST
    ┌──────────────────────────────────┐
    │ SendNotificationResponse (Response)
    ├──────────────────────────────────┤
    │ id: String (UUID from service)   │
    │ status: String (PENDING/SENT)    │
    └────────┬───────────────────────────┘
             │
             ▼
    ┌────────────────────────────────────┐
    │ NotificationResponse (Output)     │
    ├────────────────────────────────────┤
    │ id: String (UUID)                 │
    │ status: String (PENDING)          │
    │ message: String? (optional/error) │
    └────────┬─────────────────────────────┘
             │
             ▼
    ┌────────────────────────────────────┐
    │ HTTP 202 ACCEPTED                  │
    │ Content-Type: application/json     │
    └────────────────────────────────────┘
```

---

## 🎯 DECISION TREE

```
API Request
    │
    ├─ POST /notifications? 
    │  ├─ NO → 404 Not Found
    │  └─ YES ↓
    │
    ├─ Content-Type: application/json?
    │  ├─ NO → 400 Bad Request
    │  └─ YES ↓
    │
    ├─ X-API-Key header present?
    │  ├─ NO → 401 Unauthorized
    │  └─ YES ↓
    │
    ├─ X-API-Key matches NOTIFICATION_API_KEY?
    │  ├─ NO → 401 Unauthorized
    │  └─ YES ↓
    │
    ├─ Body is valid JSON?
    │  ├─ NO → 400 Bad Request
    │  └─ YES ↓
    │
    ├─ phone field valid (10-15 digits)?
    │  ├─ NO → 400 Validation Error
    │  └─ YES ↓
    │
    ├─ message field valid (1-1000 chars)?
    │  ├─ NO → 400 Validation Error
    │  └─ YES ↓
    │
    ├─ Can connect to notification-service?
    │  ├─ NO → 503 Service Unavailable
    │  └─ YES ↓
    │
    ├─ Service responds within 5s?
    │  ├─ NO → 504 Gateway Timeout
    │  └─ YES ↓
    │
    └─ Service returns success?
       ├─ NO → 503 Service Unavailable (ou código específico)
       └─ YES ↓
          
         ✅ 202 ACCEPTED
         { "id": "uuid", "status": "PENDING" }
```

---

**Visual Guide Completo! 🎨**

Use esses diagramas para entender o fluxo completo da aplicação.
