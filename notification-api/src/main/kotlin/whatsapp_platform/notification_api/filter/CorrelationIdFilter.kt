package whatsapp_platform.notification_api.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import whatsapp_platform.notification_api.context.CorrelationIdContext
import java.util.*

@Component
class CorrelationIdFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val correlationId = httpRequest.getHeader("X-Correlation-ID") ?: UUID.randomUUID().toString()

        try {
            CorrelationIdContext.set(correlationId)
            chain.doFilter(request, response)
        } finally {
            CorrelationIdContext.clear()
        }
    }
}
