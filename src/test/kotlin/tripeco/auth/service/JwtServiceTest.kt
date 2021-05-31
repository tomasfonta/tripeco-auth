package tripeco.auth.service

import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tripeco.auth.config.JwtConfig
import tripeco.auth.model.Role

@ExtendWith(MockKExtension::class)
internal class JwtServiceTest {

    private lateinit var subject: JwtService

    private val jwtConfig = JwtConfig("secret", "86400000", "Bearer")

    @BeforeEach
    internal fun setUp() {
        subject = JwtService(jwtConfig)
    }

    @Test
    fun `generate and parse token success`() {
        val userId = "userId"
        val actual = subject.generate(userId, Role.ADMIN.name)
        val parsed =  subject.parsePayload(actual)

        assert(actual.startsWith(jwtConfig.prefix))
        assertEquals(userId, parsed.userId)
        assertEquals(Role.ADMIN, parsed.role)
    }

}