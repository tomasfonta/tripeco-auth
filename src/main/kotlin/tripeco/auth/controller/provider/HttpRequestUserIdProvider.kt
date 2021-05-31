package tripeco.auth.controller.provider

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import tripeco.auth.error.TripecoException
import tripeco.auth.service.provider.UserIdProvider
import javax.servlet.http.HttpServletRequest

@Component
class HttpRequestUserIdProvider(private val request: HttpServletRequest,
                                @Value("\${tripeco.user-header}") private val header: String) : UserIdProvider {

    override fun get(): String = request.getHeader(header)
            ?: throw TripecoException.Validation(HttpStatus.BAD_REQUEST,
                    "Unable to process your request, please try again.",
                    "User Id not found in header")

}
