package whatsapp_platform.notification_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.SpringApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class NotificationApiApplication

fun main(args: Array<String>) {
    SpringApplication.run(NotificationApiApplication::class.java, *args)
}
