package whatsapp_platform.notification_api.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import whatsapp_platform.notification_api.client.NotificationServiceClient
import whatsapp_platform.notification_api.client.SendNotificationRequest
import whatsapp_platform.notification_api.context.CorrelationIdContext
import whatsapp_platform.notification_api.dto.NotificationRequest
import whatsapp_platform.notification_api.dto.NotificationResponse
import whatsapp_platform.notification_api.exception.ServiceUnavailableException
import whatsapp_platform.notification_api.exception.TimeoutException
import whatsapp_platform.notification_api.exception.UnauthorizedException
import whatsapp_platform.notification_api.logger.StructuredLogger
import java.util.*

@Service
class NotificationService(
    private val notificationServiceClient: NotificationServiceClient
) {
    private val logger = StructuredLogger.getLogger(this::class.java)

    @Value("\${notification.api.key}")
    private lateinit var apiKey: String

    @Value("\${notification.timeout.ms:5000}")
    private val timeoutMs: Long = 5000

    fun sendNotification(request: NotificationRequest): NotificationResponse {
        val correlationId = CorrelationIdContext.get()
        val requestId = UUID.randomUUID().toString()

        logger.info(
            "notification_request_received",
            "requestId" to requestId,
            "phone" to request.phone,
            "messageLength" to request.message.length
        )

        return try {
            val clientRequest = SendNotificationRequest(
                phone = request.phone,
                message = request.message,
                correlationId = correlationId
            )

            logger.info(
                "calling_notification_service",
                "requestId" to requestId,
                "phone" to request.phone
            )

            val response = notificationServiceClient.send(clientRequest, apiKey, correlationId)

            logger.info(
                "notification_sent_successfully",
                "requestId" to requestId,
                "notificationId" to response.id,
                "status" to response.status
            )

            NotificationResponse(
                id = response.id,
                status = response.status
            )
        } catch (ex: java.net.SocketTimeoutException) {
            logger.error(
                "notification_timeout",
                ex,
                "requestId" to requestId,
                "phone" to request.phone,
                "timeoutMs" to timeoutMs
            )
            throw TimeoutException()
        } catch (ex: feign.FeignException.Unauthorized) {
            logger.error(
                "unauthorized_api_key",
                ex,
                "requestId" to requestId
            )
            throw UnauthorizedException()
        } catch (ex: feign.FeignException.ServiceUnavailable) {
            logger.error(
                "notification_service_unavailable",
                ex,
                "requestId" to requestId
            )
            throw ServiceUnavailableException()
        } catch (ex: Exception) {
            logger.error(
                "notification_error",
                ex,
                "requestId" to requestId,
                "phone" to request.phone
            )
            throw ServiceUnavailableException("Failed to send notification: ${ex.message}")
        }
    }
}
