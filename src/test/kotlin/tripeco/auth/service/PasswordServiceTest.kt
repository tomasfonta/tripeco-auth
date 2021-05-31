package tripeco.auth.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

private const val TEST_PASSWORD = "12345678"
private const val REGEX = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[^\\w\\s]).{8,}\$"

internal class PasswordGeneratorTest {

    @Test
    fun `generate password for production`() {
        assertNotEquals(TEST_PASSWORD, PasswordService(true, "").generate())
    }

    @Test
    fun `generate password for non production`() {
        assertEquals(TEST_PASSWORD, PasswordService(false, "").generate())
    }

    @Test
    fun `password doesn't meet the rules`() {
        assertThrows(PasswordTooWeakException::class.java) { PasswordService(false, REGEX).validate("password")}
    }

    @Test
    fun `password meets the rules`() {
        assertDoesNotThrow { PasswordService(false, REGEX).validate("PassWord$")}
    }
}