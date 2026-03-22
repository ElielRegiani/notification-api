package whatsapp_platform.notification_api.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import whatsapp_platform.notification_api.dto.ErrorResponse
import whatsapp_platform.notification_api.logger.StructuredLogger

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = StructuredLogger.getLogger(this::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .map { "${it.field}: ${it.defaultMessage}" }
            .joinToString(", ")

        logger.warn(
            "validation_error",
            "errors" to errors,
            "path" to request.getDescription(false)
        )

        return ResponseEntity(
            ErrorResponse(
                code = "VALIDATION_ERROR",
                message = errors,
                path = request.getDescription(false)
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(NotificationException::class)
    fun handleNotificationException(
        ex: NotificationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error(
            "notification_error",
            ex,
            "code" to ex.code,
            "path" to request.getDescription(false)
        )

        return ResponseEntity(
            ErrorResponse(
                code = ex.code,
                message = ex.message,
                path = request.getDescription(false)
            ),
            HttpStatus.valueOf(ex.statusCode)
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error(
            "internal_error",
            ex,
            "path" to request.getDescription(false)
        )

        return ResponseEntity(
            ErrorResponse(
                code = "INTERNAL_ERROR",
                message = "An unexpected error occurred",
                path = request.getDescription(false)
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}
