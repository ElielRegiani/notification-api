package whatsapp_platform.notification_api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NotificationResponse(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("status")
    val status: String,

    @JsonProperty("message")
    val message: String? = null
) {
    companion object {
        fun pending(id: String): NotificationResponse = NotificationResponse(
            id = id,
            status = "PENDING"
        )

        fun error(id: String, message: String): NotificationResponse = NotificationResponse(
            id = id,
            status = "ERROR",
            message = message
        )
    }
}
