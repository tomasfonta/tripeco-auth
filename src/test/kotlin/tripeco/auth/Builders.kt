package tripeco.auth

import org.bson.types.ObjectId
import tripeco.auth.model.Role
import tripeco.auth.model.User

class UserBuilder {
    companion object {
        fun build(
            id: String = ObjectId.get().toString(),
            firstName: String = "firstName",
            lastName: String = "lastName",
            email: String = "tripeco@gmail.com",
            password: String = "12345678",
            phoneCode: String = "444444",
            address: String = "Address 1234",
            zipCode: String = "33132",
            role: Role = Role.ADMIN
        ) = User(
            id, firstName, lastName, email, password, phoneCode,
            address, zipCode, role
        )
    }
}

