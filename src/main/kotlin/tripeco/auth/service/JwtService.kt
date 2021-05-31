package tripeco.auth.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Service
import tripeco.auth.config.JwtConfig
import tripeco.auth.model.Role
import java.util.*

@Service
class JwtService(private val jwtConfig: JwtConfig) {

    fun generate(subject: String, role: String): String {
        val token = Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, jwtConfig.secret)
                .setSubject(subject)
                .setExpiration(Date(System.currentTimeMillis() + jwtConfig.expiration.toLong()))
                .claim("role", role)
                .compact()
        return "${jwtConfig.prefix} $token"
    }

    fun parsePayload(jwt: String): JwtPayload {
        val token = jwt.removePrefix(jwtConfig.prefix)
        val claims = Jwts.parser()
                .setSigningKey(jwtConfig.secret)
                .parseClaimsJws(token)
                .body
        val userId = claims.get("sub", String::class.java)
        val role = claims.get("role", String::class.java)
        return JwtPayload(userId, Role.valueOf(role))
    }

}

data class JwtPayload(val userId: String, val role: Role)