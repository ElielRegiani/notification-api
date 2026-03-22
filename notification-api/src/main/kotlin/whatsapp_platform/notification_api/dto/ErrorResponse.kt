package whatsapp_platform.notification_api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse(
    @JsonProperty("code")
    val code: String,

    @JsonProperty("message")
    val message: String,

    @JsonProperty("timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @JsonProperty("path")
    val path: String? = null
)
