package whatsapp_platform.notification_api.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

data class SendNotificationRequest(
    @JsonProperty("phone")
    val phone: String,

    @JsonProperty("message")
    val message: String,

    @JsonProperty("correlationId")
    val correlationId: String
)

data class SendNotificationResponse(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("status")
    val status: String
)

@FeignClient(
    name = "notification-service",
    url = "\${notification.service.url:http://localhost:8081}"
)
interface NotificationServiceClient {
    @PostMapping("/notifications")
    fun send(
        @RequestBody request: SendNotificationRequest,
        @RequestHeader("X-API-Key") apiKey: String,
        @RequestHeader("X-Correlation-ID") correlationId: String
    ): SendNotificationResponse
}
