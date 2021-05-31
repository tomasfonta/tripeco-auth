package tripeco.auth.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import tripeco.auth.error.ErrorResponse
import tripeco.auth.service.JwtService

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class TripecoAuthenticationFilter(
    private val jwtService: JwtService,
    private val objectMapper: ObjectMapper
) : UsernamePasswordAuthenticationFilter() {

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val credentials = jacksonObjectMapper().readValue(request.reader, LoginRequest::class.java)
        val authToken = UsernamePasswordAuthenticationToken(credentials.email, credentials.password)
        return authenticationManager.authenticate(authToken)
    }

    override fun successfulAuthentication(
        request: HttpServletRequest, response: HttpServletResponse,
        chain: FilterChain, authResult: Authentication
    ) {
        val jwt = jwtService.generate(authResult.principal as String, authResult.authorities.first().authority)
        response.addHeader(HttpHeaders.AUTHORIZATION, jwt)
    }

    override fun unsuccessfulAuthentication(
        request: HttpServletRequest, response: HttpServletResponse,
        failed: AuthenticationException
    ) {
        val body = when (failed) {
            is UsernameNotFoundException ->
                ErrorResponse("We couldn't find an user with that email. Please try again.", failed.toString())
            is DisabledException ->
                ErrorResponse("Your account is disabled. Please contact a administrator.", failed.toString())
            is BadCredentialsException ->
                ErrorResponse("The email or password are not valid. Please check and try again.", failed.toString())
            else -> ErrorResponse("Unexpected error. Please try again.", failed.toString())
        }
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = "application/json"
        response.writer.write(objectMapper.writeValueAsString(body))
        response.flushBuffer()
    }

    @Autowired
    override fun setAuthenticationManager(authenticationManager: AuthenticationManager) {
        super.setAuthenticationManager(authenticationManager)
    }
}

data class LoginRequest(val email: String, val password: String)
