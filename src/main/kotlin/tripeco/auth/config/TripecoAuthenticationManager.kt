package tripeco.auth.config

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import tripeco.auth.model.Role
import tripeco.auth.repository.UserRepository

@Component
class TripecoAuthenticationManager(private val userRepository: UserRepository,
                                   private val encoder: BCryptPasswordEncoder) : AuthenticationManager {

    override fun authenticate(auth: Authentication): Authentication {
        val email = auth.name
        val password = auth.credentials.toString()
        val user = userRepository.findByEmailIgnoreCase(email)
            ?: throw UsernameNotFoundException("User with email=$email not found")
        val authUser = AuthUser(user.id!!, user.email, user.password, user.role)
        return if (encoder.matches(password, authUser.password)) {
            UsernamePasswordAuthenticationToken(authUser.id, authUser.password, listOf(SimpleGrantedAuthority(authUser.role.name)))
        } else {
            throw BadCredentialsException("Invalid password")
        }
    }

}

data class AuthUser(val id: String, val email: String, val password: String, val role: Role)
