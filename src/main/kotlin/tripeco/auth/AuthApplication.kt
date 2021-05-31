package tripeco.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class AuthApplication

fun main(args: Array<String>) {
	runApplication<AuthApplication>(*args)
}
