package whatsapp_platform.notification_api.exception

sealed class NotificationException(
    open val code: String,
    open val statusCode: Int,
    override val message: String
) : RuntimeException(message)

data class InvalidPhoneException(
    override val message: String = "Phone is invalid"
) : NotificationException(
    code = "INVALID_PHONE",
    statusCode = 400,
    message = message
)

data class InvalidMessageException(
    override val message: String = "Message is invalid or empty"
) : NotificationException(
    code = "INVALID_MESSAGE",
    statusCode = 400,
    message = message
)

data class UnauthorizedException(
    override val message: String = "Unauthorized - Invalid API Key"
) : NotificationException(
    code = "UNAUTHORIZED",
    statusCode = 401,
    message = message
)

data class ServiceUnavailableException(
    override val message: String = "Notification service is temporarily unavailable"
) : NotificationException(
    code = "SERVICE_UNAVAILABLE",
    statusCode = 503,
    message = message
)

data class TimeoutException(
    override val message: String = "Request timeout - notification service did not respond in time"
) : NotificationException(
    code = "TIMEOUT",
    statusCode = 504,
    message = message
)

data class RateLimitExceededException(
    override val message: String = "Rate limit exceeded"
) : NotificationException(
    code = "RATE_LIMIT_EXCEEDED",
    statusCode = 429,
    message = message
)
