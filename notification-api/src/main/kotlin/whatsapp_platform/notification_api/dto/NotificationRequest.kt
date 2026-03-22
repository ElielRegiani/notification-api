package whatsapp_platform.notification_api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class NotificationRequest(
    @field:NotBlank(message = "Phone cannot be blank")
    @field:Pattern(
        regexp = "^\\d{10,15}$",
        message = "Phone must be a valid number with 10-15 digits (e.g., 5511999999999)"
    )
    val phone: String,

    @field:NotBlank(message = "Message cannot be blank")
    @field:Size(min = 1, max = 1000, message = "Message must be between 1 and 1000 characters")
    val message: String
)
