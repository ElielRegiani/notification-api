package whatsapp_platform.notification_api.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import whatsapp_platform.notification_api.dto.NotificationRequest
import whatsapp_platform.notification_api.dto.NotificationResponse
import whatsapp_platform.notification_api.exception.UnauthorizedException
import whatsapp_platform.notification_api.logger.StructuredLogger
import whatsapp_platform.notification_api.service.NotificationService
import org.springframework.beans.factory.annotation.Value

@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {
    private val logger = StructuredLogger.getLogger(this::class.java)

    @Value("\${notification.api.key}")
    private lateinit var validApiKey: String

    @PostMapping
    fun sendNotification(
        @Valid @RequestBody request: NotificationRequest,
        @RequestHeader(value = "X-API-Key", required = true) apiKey: String
    ): ResponseEntity<NotificationResponse> {
        // Validate API Key
        if (apiKey != validApiKey) {
            logger.warn(
                "invalid_api_key_attempt",
                "provided_key_length" to apiKey.length
            )
            throw UnauthorizedException()
        }

        logger.info(
            "notification_request_started",
            "phone" to request.phone
        )

        val response = notificationService.sendNotification(request)

        return ResponseEntity(response, HttpStatus.ACCEPTED)
    }
}
