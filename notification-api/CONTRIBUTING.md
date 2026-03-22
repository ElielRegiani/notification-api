# 🤝 CONTRIBUTING

Guia para contribuir com a notification-api.

## 📋 Antes de Começar

1. Faça fork do repositório
2. Clone localmente: `git clone https://github.com/seu-usuario/notification-api.git`
3. Crie uma branch: `git checkout -b feature/xyz`
4. Leia este guia

## 🛠️ Setup Local

```bash
# Clone
git clone ...
cd notification-api

# Configure .env
cp .env.example .env
# Edite .env com valores locais

# Build
./gradlew clean build

# Testes
./gradlew test

# Run
./gradlew bootRun
```

## ✍️ Convenções de Código

### Kotlin Style

```kotlin
// ✅ BOM
class NotificationService(
    private val client: NotificationServiceClient
) {
    fun sendNotification(request: NotificationRequest): NotificationResponse {
        // Implementation
    }
}

// ❌ RUIM
class NotificationService {
    private var client: NotificationServiceClient? = null
    fun send(req: NotificationRequest?): NotificationResponse? {
        // ...
    }
}
```

### Naming

- Classes: PascalCase
- Functions: camelCase
- Constants: UPPER_CASE
- Private members: `_prefix` (opcional)

### Comments

```kotlin
// ✅ BOM: Explica WHY, não WHAT
// Exponencial backoff para retry com jitter
val delay = baseDelay * Math.pow(2.0, attempt.toDouble()).toLong()

// ❌ RUIM: Comentário óbvio
// Incrementa o contador
counter++
```

## 📝 Commit Messages

Use o padrão Conventional Commits:

```
feat: add rate limiting middleware
fix: handle timeout correctly in notification service
docs: update deployment guide
refactor: simplify error handling
test: add integration tests for controller
chore: update dependencies
```

## 🧪 Testing

Antes de fazer commit:

```bash
# Rode todos os testes
./gradlew test

# Build completo
./gradlew clean build

# Verifica style
./gradlew lintKotlin  (se configurado)
```

### Exemplos de Testes

```kotlin
@Test
fun `should validate phone format`() {
    val request = NotificationRequest(
        phone = "123",  // Inválido
        message = "Test"
    )
    
    mockMvc.post("/api/v1/notifications") {
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(request)
    }.andExpect {
        status { isBadRequest() }
        jsonPath("$.code") { value("VALIDATION_ERROR") }
    }
}
```

## 📦 PR Checklist

Antes de fazer push:

- [ ] Testes passam: `./gradlew test`
- [ ] Build sem erros: `./gradlew clean build`
- [ ] Sem hardcoded secrets ou API keys
- [ ] Logs estruturados adicionados se necessário
- [ ] Documentação atualizada (README, ARCHITECTURE)
- [ ] Commit message segue convenção

## 🎯 Tipos de Contribuição

### 🐛 Bug Fixes

```
git checkout -b fix/issue-description
# Faça o fix
# Adicione teste que reproduz o bug
git commit -m "fix: description"
git push origin fix/issue-description
```

### ✨ Features Novas

```
git checkout -b feature/nova-feature
# Implemente
# Adicione testes
# Atualize documentação
git commit -m "feat: nova-feature"
git push origin feature/nova-feature
```

### 📚 Documentação

```
git checkout -b docs/update-something
# Atualize .md files
git commit -m "docs: update something"
git push origin docs/update-something
```

### 🔄 Refactoring

```
git checkout -b refactor/simplify-service
# Refatore
# Execute testes
git commit -m "refactor: simplify notification service"
git push origin refactor/simplify-service
```

## 🔍 Code Review

Esperamos que PRs tenham:

1. **Descrição clara** do que foi mudado
2. **Testes** cobrindo o novo código
3. **Documentação** atualizada se necessário
4. **Commits bem estruturados** com mensagens descritivas

### Review Checklist

- ✅ Código segue estilo do projeto
- ✅ Sem duplicação desnecessária
- ✅ Testes adequados
- ✅ Sem breaking changes (ou bem documentado)
- ✅ Performance aceitável
- ✅ Sem secrets em código

## 📊 Arquivos Importantes

- `src/main/kotlin/`: Código source
- `src/test/kotlin/`: Testes
- `src/main/resources/application.yaml`: Configuração
- `build.gradle.kts`: Dependências
- `Dockerfile`: Definição da imagem
- `README.md`: Documentação principal
- `ARCHITECTURE.md`: Arquitetura técnica
- `DEPLOYMENT.md`: Guia de deployment

## 🚀 Features Planejadas

Veja issues abertas para features que queremos:

- [ ] Rate limiting
- [ ] Caching
- [ ] Metrics (Prometheus)
- [ ] OpenAPI/Swagger
- [ ] Distributed tracing
- [ ] Circuit breaker

## 💬 Dúvidas?

1. Abra uma discussão
2. Consulte `ARCHITECTURE.md`
3. Olhe exemplos de PRs anteriores

## ✅ Parabéns!

Se sua contribuição for aceita, você será:

1. ✅ Adicionado à `CONTRIBUTORS.md`
2. ✅ Creditado no changelog
3. ✅ Mencionado no release notes

---

**Obrigado por contribuir! 🙏**
