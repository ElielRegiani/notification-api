package whatsapp_platform.notification_api.config

import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import whatsapp_platform.notification_api.context.CorrelationIdContext
import whatsapp_platform.notification_api.logger.StructuredLogger

@Configuration
class FeignConfig {
    private val logger = StructuredLogger.getLogger(this::class.java)

    @Bean
    fun correlationIdInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val correlationId = CorrelationIdContext.get()
            template.header("X-Correlation-ID", correlationId)
            logger.info(
                "feign_request_prepared",
                mapOf(
                    "method" to template.method(),
                    "url" to template.url(),
                    "correlationId" to correlationId
                )
            )
        }
    }
}
