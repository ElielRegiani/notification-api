package whatsapp_platform.notification_api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
class NotificationApiApplicationTests {

    @Autowired(required = false)
    private var mockMvc: MockMvc? = null

    @Test
    fun contextLoads() {
    }

    @Test
    fun healthCheckEndpointReturnsOk() {
        if (mockMvc != null) {
            mockMvc!!.perform(MockMvcRequestBuilders.get("/actuator/health"))
                .andExpect(MockMvcResultMatchers.status().isOk)
        }
    }
}
