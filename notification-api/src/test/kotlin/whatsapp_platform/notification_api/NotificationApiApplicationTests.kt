package whatsapp_platform.notification_api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.http.MediaType

@SpringBootTest
@AutoConfigureMockMvc
class NotificationApiApplicationTests {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Test
	fun contextLoads() {
	}

	@Test
	fun `should return UP health status`() {
		mockMvc.get("/api/v1/actuator/health")
			.andExpect {
				status { isOk() }
				jsonPath("$.status") { value("UP") }
				jsonPath("$.service") { value("notification-api") }
			}
	}

	@Test
	fun `should reject notification without API key`() {
		mockMvc.post("/api/v1/notifications") {
			contentType = MediaType.APPLICATION_JSON
			content = """{"phone": "5511999999999", "message": "Test"}"""
		}.andExpect {
			status { isUnauthorized() }
		}
	}

	@Test
	fun `should reject invalid phone`() {
		mockMvc.post("/api/v1/notifications") {
			contentType = MediaType.APPLICATION_JSON
			header("X-API-Key", "sk_test_123456789")
			content = """{"phone": "123", "message": "Test"}"""
		}.andExpect {
			status { isBadRequest() }
			jsonPath("$.code") { value("VALIDATION_ERROR") }
		}
	}

	@Test
	fun `should reject empty message`() {
		mockMvc.post("/api/v1/notifications") {
			contentType = MediaType.APPLICATION_JSON
			header("X-API-Key", "sk_test_123456789")
			content = """{"phone": "5511999999999", "message": ""}"""
		}.andExpect {
			status { isBadRequest() }
			jsonPath("$.code") { value("VALIDATION_ERROR") }
		}
	}
}
