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
import java.util.UUID

@Service
class NotificationService(
    private val notificationServiceClient: NotificationServiceClient
) {
    private val logger = StructuredLogger.getLogger(NotificationService::class.java)

    @Value("\${notification.api.key}")
    var apiKey: String = ""

    @Value("\${notification.timeout.ms:5000}")
    var timeoutMs: Long = 5000

    fun sendNotification(request: NotificationRequest): NotificationResponse {
        val correlationId = CorrelationIdContext.get()
        val requestId = UUID.randomUUID().toString()

        logger.info(
            "notification_request_received",
            mapOf<String, Any?>(
                Pair("requestId", requestId),
                Pair("phone", request.phone),
                Pair("messageLength", request.message.length)
            )
        )

        return try {
            val clientRequest = SendNotificationRequest(
                phone = request.phone,
                message = request.message,
                correlationId = correlationId
            )

            logger.info(
                "calling_notification_service",
                mapOf<String, Any?>(
                    Pair("requestId", requestId),
                    Pair("phone", request.phone)
                )
            )

            val response = notificationServiceClient.send(clientRequest, apiKey, correlationId)

            logger.info(
                "notification_sent_successfully",
                mapOf<String, Any?>(
                    Pair("requestId", requestId),
                    Pair("notificationId", response.id),
                    Pair("status", response.status)
                )
            )

            NotificationResponse(
                id = response.id,
                status = response.status
            )
        } catch (ex: java.net.SocketTimeoutException) {
            logger.error(
                "notification_timeout",
                ex,
                mapOf<String, Any?>(
                    Pair("requestId", requestId),
                    Pair("phone", request.phone),
                    Pair("timeoutMs", timeoutMs)
                )
            )
            throw TimeoutException()
        } catch (ex: feign.FeignException.Unauthorized) {
            logger.error(
                "unauthorized_api_key",
                ex,
                mapOf<String, Any?>(
                    Pair("requestId", requestId)
                )
            )
            throw UnauthorizedException()
        } catch (ex: feign.FeignException.ServiceUnavailable) {
            logger.error(
                "notification_service_unavailable",
                ex,
                mapOf<String, Any?>(
                    Pair("requestId", requestId)
                )
            )
            throw ServiceUnavailableException()
        } catch (ex: Exception) {
            logger.error(
                "notification_error",
                ex,
                mapOf<String, Any?>(
                    Pair("requestId", requestId),
                    Pair("phone", request.phone)
                )
            )
            throw ServiceUnavailableException("Failed to send notification: ${'$'}{ex.message}")
        }
    }
}
