package whatsapp_platform.notification_api.logger

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import whatsapp_platform.notification_api.context.CorrelationIdContext

class StructuredLogger(private val clazz: Class<*>) {
    private val logger: Logger = LoggerFactory.getLogger(clazz)

    fun info(event: String, pairs: Map<String, Any?>? = null) {
        val logMessage = buildLogMessage(event, pairs)
        logger.info(logMessage)
    }

    fun error(event: String, exception: Exception? = null, pairs: Map<String, Any?>? = null) {
        val logMessage = buildLogMessage(event, pairs, exception?.message)
        if (exception != null) {
            logger.error(logMessage, exception)
        } else {
            logger.error(logMessage)
        }
    }

    fun warn(event: String, pairs: Map<String, Any?>? = null) {
        val logMessage = buildLogMessage(event, pairs)
        logger.warn(logMessage)
    }

    private fun buildLogMessage(event: String, pairs: Map<String, Any?>?, exception: String? = null): String {
        val sb = StringBuilder()
        sb.append("[correlationId=${CorrelationIdContext.get()}] ")
        sb.append("[event=$event] ")
        if (pairs != null) {
            for (entry in pairs.entries) {
                sb.append("[${entry.key}=${entry.value}] ")
            }
        }
        if (exception != null) {
            sb.append("[exception=$exception]")
        }
        return sb.toString()
    }

    companion object {
        fun getLogger(clazz: Class<*>): StructuredLogger = StructuredLogger(clazz)
    }
}
