package tripeco.auth.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import tripeco.auth.error.TripecoException
import tripeco.auth.repository.UserRepository
import tripeco.auth.service.provider.UserIdProvider
import tripeco.auth.UserBuilder
import tripeco.auth.model.Role
import tripeco.auth.model.User
import java.util.*

@ExtendWith(MockKExtension::class)
internal class UserServiceTest {

    @InjectMockKs
    private lateinit var subject: UserService

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var encoder: BCryptPasswordEncoder

    @MockK
    private lateinit var emailService: EmailService

    @MockK
    private lateinit var userIdProvider: UserIdProvider

    @MockK
    private lateinit var passwordService: PasswordService

    private val password = "password"
    private val encodedPassword = "encodedPassword"

    @BeforeEach
    internal fun setUp() {
        every { passwordService.generate() } returns password
        every { encoder.encode(any()) } returns encodedPassword
    }

    @Test
    fun `register non customer neither laboratory user success`() {
        val userDto = RegisterUserRequest("firstName", "lastName", "email", "phoneCode",
                "address", "zipCode", Role.ADMIN)
        val userSaved = User(
                id = "userId",
                email = userDto.email,
                password = encodedPassword,
                firstName = userDto.firstName,
                lastName = userDto.lastName,
                address = userDto.address,
                zipCode = userDto.zipCode,
                phoneCode = userDto.phoneCode,
                role = userDto.role

                )
        val userSlot = slot<User>()
        every { userRepository.existsByEmailIgnoreCase(userDto.email) } returns false
        every { userRepository.save(capture(userSlot)) } returns userSaved
        every { emailService.newUser(userSaved, any()) } just Runs

        subject.register(userDto)

        verify {
            encoder.encode(any())
            userRepository.save(userSlot.captured)
            emailService.newUser(eq(userSaved), any())
        }
    }

    @Test
    fun `register customer user success`() {
        val userDto = RegisterUserRequest("firstName", "lastName", "email", "phoneCode",
                "address", "zipCode", Role.USER)
        val userSaved = User(
                id = "userId",
                email = userDto.email,
                password = encodedPassword,
                firstName = userDto.firstName,
                lastName = userDto.lastName,
                address = userDto.address,
                zipCode = userDto.zipCode,
                phoneCode = userDto.phoneCode,
                role = userDto.role
                )
        val userSlot = slot<User>()
        every { userRepository.existsByEmailIgnoreCase(userDto.email) } returns false
        every { userRepository.save(capture(userSlot)) } returns userSaved
        every { emailService.newUser(eq(userSaved), any()) } just Runs

        subject.register(userDto)

        verify {
            encoder.encode(any())
            userRepository.save(userSlot.captured)
            emailService.newUser(eq(userSaved), any())
        }
    }


    @Test
    fun `register existing email fail`() {
        val userDto = RegisterUserRequest("firstName", "lastName", "email", "phoneCode",
                "address", "zipCode", Role.USER)
        every { userRepository.existsByEmailIgnoreCase(userDto.email) } returns true

        assertThrows(
            TripecoException.Validation::class.java, { subject.register(userDto) },
                "User with email=${userDto.email} already exists")
    }

    @Test
    fun `getCurrentResponse success`() {
        val userId = "userId"
        val idSlot = slot<String>()
        val user = UserBuilder.build( role = Role.ADMIN)
        val userResponse = user.toCurrent()
        every { userIdProvider.get() } returns userId
        every { userRepository.findById(capture(idSlot)) } returns Optional.of(user)

        assertEquals(userResponse, subject.getCurrentResponse())
        assertEquals(userId, idSlot.captured)
    }

    @Test
    fun `getLoggedUser user not found`() {
        val userId = "userId"
        every { userIdProvider.get() } returns userId
        every { userRepository.findById(userId) } returns Optional.empty()

        assertThrows(UserNotFoundException::class.java) { subject.getCurrent() }
    }

    @Test
    fun `findById found test`() {
        val id = "id"
        val user = UserBuilder.build(role = Role.USER)
        every { userRepository.findById(id) } returns Optional.of(user)

        assertEquals(user.toResponse(), subject.findById(id))
    }

    @Test
    fun `findBy not found test`() {
        val id = "id"
        every { userRepository.findById(id) } returns Optional.empty()

        assertThrows(UserNotFoundException::class.java) { subject.findById(id) }
    }

    @Test
    fun `findAll test`() {
        val user = UserBuilder.build(role = Role.USER)
        every { userRepository.findAll() } returns listOf(user)

        assertEquals(listOf(user.toResponse()), subject.findAll())
    }

    @Test
    fun `findByRole test`() {
        val role = Role.USER
        val user = UserBuilder.build(role = Role.USER)
        every { userRepository.findByRole(role) } returns listOf(user)

        assertEquals(listOf(user.toResponse()), subject.findByRole(role))
    }

    @Test
    fun `update existing user`() {
        val id = "id"
        val user = UserBuilder.build(role = Role.ADMIN)
        val userChanged = UserBuilder.build(firstName = "firstNameChanged", lastName = "lastNameChanged",
                phoneCode = "phoneCodeChanged", address = "addressChanged", zipCode = "zipCodeChanged",
                role = Role.ADMIN)
        val userRequest = UpdateUserRequest("firstNameChanged", "lastNameChanged", "phoneCodeChanged",
                "addressChanged", "zipCodeChanged", Role.ADMIN)
        every { userRepository.findById(id) } returns Optional.of(user)
        every { userRepository.save(user) } returns userChanged

        assertEquals(subject.update(id, userRequest), userChanged.toResponse())
    }


    @Test
    fun `update nonexisting user`() {
        val id = "userId"
        val user = UpdateUserRequest("firstName", "lastName", "phoneCode", "address", "zipCode", Role.ADMIN)
        every { userRepository.findById(id) } returns Optional.empty()

        assertThrows(TripecoException.Validation::class.java) { subject.update(id, user) }
    }

    @Test
    fun `delete user`() {
        val userId = "userId"
        every { userRepository.deleteById(userId) } just Runs
        subject.delete(userId)
        verify { userRepository.deleteById(userId) }
    }

    @Test
    fun `change password successfully`() {
        val request = ChangePasswordRequest("oldPassword$", "New!Password")
        val user = UserBuilder.build(password = request.newPassword, role = Role.ADMIN)
        every { passwordService.validate(request.newPassword) } just Runs
        every { userRepository.save(user) } returns user
        every { userIdProvider.get() } returns "userId"
        every { encoder.matches(request.oldPassword, user.password) } returns true
        every { userRepository.findByIdOrNull("userId") } returns user

        subject.changePassword(request)
        verify { userRepository.save(user) }
    }

    @Test
    fun `change password with wrong old password`() {
        val request = ChangePasswordRequest("wrongOldPassword$", "New!Password")
        val user = UserBuilder.build(password = request.newPassword, role = Role.ADMIN)
        every { passwordService.validate(request.newPassword) } just Runs
        every { userIdProvider.get() } returns "userId"
        every { encoder.matches(request.oldPassword, user.password) } returns false
        every { userRepository.findByIdOrNull("userId") } returns user

        assertThrows(TripecoException.Validation::class.java) { subject.changePassword(request) }
    }

    @Test
    fun `change password with weak new password`() {
        val request = ChangePasswordRequest("wrongOldPassword$", "newweakpassword")
        every { passwordService.validate(request.newPassword) } throws PasswordTooWeakException()

        assertThrows(PasswordTooWeakException::class.java) { subject.changePassword(request) }
    }


    @Test
    fun `resend password `() {
        val id = "id"
        val user = UserBuilder.build(id = id, role = Role.USER)
        val userSlot = slot<User>()
        every { userRepository.findById(id) } returns Optional.of(user)
        every { userRepository.save(capture(userSlot)) } returns user

        every { emailService.newUser(eq(user), any()) } just Runs

        subject.resendPassword(id)

        assertEquals(encodedPassword, userSlot.captured.password)
        verify {
            encoder.encode(any())
            userRepository.save(userSlot.captured)
            emailService.newUser(eq(user), any())
        }
    }

}
