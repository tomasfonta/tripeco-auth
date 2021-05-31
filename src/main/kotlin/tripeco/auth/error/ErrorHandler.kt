package tripeco.auth.error

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.Instant
import java.util.*
import java.util.function.Consumer


@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
class ErrorHandler {

    @ExceptionHandler(TripecoException.Validation::class)
    fun handleValidationException(e: TripecoException.Validation) =
            ResponseEntity(ErrorResponse(e.errorMessage, e.developerMessage, type = e.type, showAs = e.showAs), e.status)

    @ExceptionHandler(TripecoException.Unexpected::class)
    fun handleUnexpectedException(e: TripecoException.Unexpected) =
            ResponseEntity(ErrorResponse(e.errorMessage, e.cause.toString()), e.status)

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: RuntimeException) =
            ResponseEntity(
                ErrorResponse("Unable to process your request, please try again.", e.toString()),
                    HttpStatus.INTERNAL_SERVER_ERROR)


    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
            ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors: MutableMap<String, String?> = HashMap()
        ex.bindingResult.allErrors.forEach(Consumer { error: ObjectError ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.getDefaultMessage()
            errors[fieldName] = errorMessage
        })
        val currentErrors = errors.map { " ${it.key}: ${it.value} " }.joinToString()

        return ResponseEntity(
            ErrorResponse("Unable to process your request, please try again.", currentErrors),
                HttpStatus.UNPROCESSABLE_ENTITY)

    }
}

sealed class TripecoException {

    open class Validation(val status: HttpStatus,
                          val errorMessage: String,
                          val developerMessage: String? = null,
                          val type: ValidationType? = ValidationType.ERROR,
                          val showAs: ValidationShowAs? = ValidationShowAs.SNACKBAR
    ) : RuntimeException()

    open class Unexpected(val status: HttpStatus,
                          val errorMessage: String,
                          cause: Throwable) : RuntimeException(cause)

}

data class ErrorResponse(val errorMessage: String,
                         val developerMessage: String?,
                         val timestamp: Instant = Instant.now(),
                         val type: ValidationType? = ValidationType.ERROR,
                         val showAs: ValidationShowAs? = ValidationShowAs.SNACKBAR
)

enum class ValidationType {
    ERROR
}

enum class ValidationShowAs {
     SNACKBAR
}
