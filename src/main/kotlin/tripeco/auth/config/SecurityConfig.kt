package tripeco.auth.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@EnableWebSecurity
@Configuration
class SecurityConfig(private val tripecoAuthenticationFilter: TripecoAuthenticationFilter) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
                .csrf().disable()
                .formLogin().disable()
                .cors().disable()
                .addFilterBefore(tripecoAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
                .authorizeRequests().anyRequest().permitAll()
    }

}