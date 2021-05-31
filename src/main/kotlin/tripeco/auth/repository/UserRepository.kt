package tripeco.auth.repository

import org.springframework.data.mongodb.repository.MongoRepository
import tripeco.auth.model.Role
import tripeco.auth.model.User


interface UserRepository : MongoRepository<User, String> {
    fun findByEmailIgnoreCase(email: String): User?
    fun findByRole(role: Role): List<User>
    fun existsByEmailIgnoreCase(email: String): Boolean
}
