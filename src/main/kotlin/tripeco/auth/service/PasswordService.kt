package tripeco.auth.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import tripeco.auth.error.TripecoException

private const val PASSWORD_LENGTH = 8
private const val TEST_PASSWORD = "12345678"

@Component
class PasswordService(@Value("\${production}") private val production: Boolean,
                      @Value("\${tripeco.password-regex}") private val passwordRegex: String) {

    fun generate(): String = if (production) {
        "CHANGE THIS TO RAMDOM STRING REGEX"
    } else {
        TEST_PASSWORD
    }

    fun validate(requestPassword: String) {
        this.passwordRegex.toRegex().matchEntire(requestPassword) ?: throw PasswordTooWeakException()
    }
}

class PasswordTooWeakException: TripecoException.Validation(HttpStatus.UNPROCESSABLE_ENTITY,
        "New password should have at least 8 characters and uppercase, lowercase and symbol.",
        "Password doesn't meet password rules.")
