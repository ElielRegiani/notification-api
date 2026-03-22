package whatsapp_platform.notification_api.logger

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import whatsapp_platform.notification_api.context.CorrelationIdContext
import com.fasterxml.jackson.databind.ObjectMapper

class StructuredLogger(private val clazz: Class<*>) {
    private val logger: Logger = LoggerFactory.getLogger(clazz)
    private val objectMapper = ObjectMapper()

    fun info(event: String, vararg pairs: Pair<String, Any?>) {
        val logMap = mutableMapOf(
            "correlationId" to CorrelationIdContext.get(),
            "event" to event,
            "timestamp" to System.currentTimeMillis()
        )
        pairs.forEach { (key, value) -> logMap[key] = value }
        logger.info(objectMapper.writeValueAsString(logMap))
    }

    fun error(event: String, exception: Exception? = null, vararg pairs: Pair<String, Any?>) {
        val logMap = mutableMapOf(
            "correlationId" to CorrelationIdContext.get(),
            "event" to event,
            "timestamp" to System.currentTimeMillis()
        )
        pairs.forEach { (key, value) -> logMap[key] = value }
        if (exception != null) {
            logMap["exception"] = exception.message
            logger.error(objectMapper.writeValueAsString(logMap), exception)
        } else {
            logger.error(objectMapper.writeValueAsString(logMap))
        }
    }

    fun warn(event: String, vararg pairs: Pair<String, Any?>) {
        val logMap = mutableMapOf(
            "correlationId" to CorrelationIdContext.get(),
            "event" to event,
            "timestamp" to System.currentTimeMillis()
        )
        pairs.forEach { (key, value) -> logMap[key] = value }
        logger.warn(objectMapper.writeValueAsString(logMap))
    }

    companion object {
        fun getLogger(clazz: Class<*>): StructuredLogger = StructuredLogger(clazz)
    }
}
