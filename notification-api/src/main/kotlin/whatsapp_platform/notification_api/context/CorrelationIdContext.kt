package whatsapp_platform.notification_api.context

import java.util.*

object CorrelationIdContext {
    private val threadLocal = ThreadLocal<String>()

    fun set(correlationId: String) {
        threadLocal.set(correlationId)
    }

    fun get(): String {
        return threadLocal.get() ?: UUID.randomUUID().toString()
    }

    fun clear() {
        threadLocal.remove()
    }
}
