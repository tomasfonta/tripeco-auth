package tripeco.auth.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(@Id val id: String? = null,
                     var firstName: String,
                     var lastName: String,
                     @Indexed(unique = true) var email: String,
                     var password: String,
                     var phoneCode: String,
                     var address: String,
                     var zipCode: String,
                     var role: Role
)

enum class Role {
    ADMIN, USER;

    fun isUser(): Boolean = this == USER

    fun isAdmin(): Boolean = this == ADMIN

}
