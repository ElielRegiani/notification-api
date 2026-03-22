package whatsapp_platform.notification_api.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/actuator")
class HealthController {

    @GetMapping("/health")
    fun health(): HealthResponse {
        return HealthResponse(
            status = "UP",
            timestamp = System.currentTimeMillis(),
            service = "notification-api"
        )
    }
}

data class HealthResponse(
    val status: String,
    val timestamp: Long,
    val service: String
)
