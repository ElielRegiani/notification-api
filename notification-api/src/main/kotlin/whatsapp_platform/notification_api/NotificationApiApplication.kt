package whatsapp_platform.notification_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class NotificationApiApplication

fun main(args: Array<String>) {
	runApplication<NotificationApiApplication>(*args)
}
