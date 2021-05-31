package tripeco.auth.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import tripeco.auth.error.TripecoException
import tripeco.auth.repository.UserRepository
import tripeco.auth.service.provider.UserIdProvider
import tripeco.auth.model.Role
import tripeco.auth.model.User

@Service
class UserService(
    private val userRepository: UserRepository,
    private val encoder: BCryptPasswordEncoder,
    private val userIdProvider: UserIdProvider,
    private val emailService: EmailService,
    private val passwordService: PasswordService
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun register(request: RegisterUserRequest): UserResponse {
        if (userRepository.existsByEmailIgnoreCase(request.email)) {
            throw TripecoException.Validation(
                HttpStatus.UNPROCESSABLE_ENTITY, "The email is already in use.",
                "User with email=${request.email} already exists"
            )
        }
        val password = passwordService.generate()
        val user = userRepository.save(
            User(
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                password = encoder.encode(password),
                phoneCode = request.phoneCode,
                address = request.address,
                zipCode = request.zipCode,
                role = request.role
            )
        )
        emailService.newUser(user, password)
        return user.toResponse()
    }

    fun findById(id: String): UserResponse = userRepository.findById(id)
        .orElseThrow { UserNotFoundException(id) }.toResponse()

    fun findAll(): List<UserResponse> = userRepository.findAll().map { it.toResponse() }

    fun findByRole(role: Role): List<UserResponse> = userRepository.findByRole(role).map { it.toResponse() }

    fun getCurrent(): User = userRepository.findById(userIdProvider.get())
        .orElseThrow {
            logger.error("Unable to find User id=${userIdProvider.get()}")
            UserNotFoundException(userIdProvider.get())
        }

    fun getCurrentResponse(): UserCurrent = getCurrent().toCurrent()

    fun delete(id: String) {
        userRepository.deleteById(id)
    }

    fun update(id: String, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findByIdOrNull(id) ?: throw UserNotFoundException(id)
        request.firstName?.let { user.firstName = it }
        request.lastName?.let { user.lastName = it }
        request.phoneCode?.let { user.phoneCode = it }
        request.address?.let { user.address = it }
        request.zipCode?.let { user.zipCode = it }
        request.role?.let { user.role = it }
        return userRepository.save(user).toResponse()
    }

    fun changePassword(changePasswordRequest: ChangePasswordRequest) {
        passwordService.validate(changePasswordRequest.newPassword)
        val userId = userIdProvider.get()
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException(userId)
        if (encoder.matches(changePasswordRequest.oldPassword, user.password)) {
            user.password = encoder.encode(changePasswordRequest.newPassword)
            userRepository.save(user).toResponse()
        } else throw TripecoException.Validation(
            HttpStatus.BAD_REQUEST,
            "Your old password is incorrect.", "Old password sent is incorrect."
        )
    }

    fun resendPassword(id: String) {
        val user = userRepository.findByIdOrNull(id) ?: throw UserNotFoundException(id)
        val password = passwordService.generate()
        user.password = encoder.encode(password)
        userRepository.save(user)
        emailService.newUser(user, password)
    }

}

fun User.toResponse() = UserResponse(
    this.id!!, this.email, this.firstName, this.lastName, this.phoneCode,
    this.address, this.zipCode, this.role
)

fun User.toCurrent() = UserCurrent(this.id!!, this.email, this.firstName, this.lastName, this.role)

data class RegisterUserRequest(
    val firstName: String, val lastName: String, val email: String,
    val phoneCode: String, val address: String, val zipCode: String, val role: Role
)

data class UserResponse(
    val id: String, val email: String, val firstName: String, val lastName: String,
    val phoneCode: String, val address: String, val zipCode: String, val role: Role
)

data class UserCurrent(
    val id: String, val email: String, val firstName: String, val lastName: String,
    val role: Role
)

data class UpdateUserRequest(
    val firstName: String?, val lastName: String?, val phoneCode: String?,
    val address: String?, val zipCode: String?, val role: Role?
)


class UserNotFoundException(id: String) : TripecoException.Validation(
    HttpStatus.NOT_FOUND,
    "User not found. Try again.",
    "User with id= $id not found"
)

data class ChangePasswordRequest(val oldPassword: String, val newPassword: String)

data class ResendPasswordRequest(val email: String)

