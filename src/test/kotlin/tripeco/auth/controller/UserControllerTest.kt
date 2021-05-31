package tripeco.auth.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tripeco.auth.config.TripecoAuthenticationFilter
import tripeco.auth.config.SecurityConfig
import tripeco.auth.error.TripecoException
import tripeco.auth.model.Role
import tripeco.auth.UserBuilder
import tripeco.auth.service.*

@WebMvcTest(
        controllers = [UserController::class],
        excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [SecurityConfig::class, TripecoAuthenticationFilter::class])])
@AutoConfigureMockMvc(addFilters = false)
internal class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userService: UserService


    @Test
    fun `register success`() {
        val userDto = RegisterUserRequest("firstName", "lastName", "email@email.com",
                "phoneCode", "address", "zipCode", Role.ADMIN)

        mockMvc.perform(post("/users")
                .contentType("application/json")
                .content(jacksonObjectMapper().writeValueAsString(userDto)))
                .andExpect(status().isCreated)
    }

    @Test
    fun `get current response success`() {
        val jwt = "jwt"
        val user = UserBuilder.build(role = Role.USER).toCurrent()
        `when`(userService.getCurrentResponse()).thenReturn(user)

        val result = mockMvc.perform(get("/users/current")
                .header(HttpHeaders.AUTHORIZATION, jwt))
                .andExpect(status().isOk)
                .andReturn()

        assertEquals(jacksonObjectMapper().readValue(result.response.contentAsString, UserCurrent::class.java),
                user)
    }

    @Test
    fun `getAll success`() {
        val allUsers = listOf(UserBuilder.build(role = Role.ADMIN).toResponse())
        `when`(userService.findAll()).thenReturn(allUsers)

        val result = mockMvc.perform(get("/users"))
                .andExpect(status().isOk)
                .andReturn()

        assertEquals(result.response.contentAsString, jacksonObjectMapper().writeValueAsString(allUsers))
    }

    @Test
    fun `getAll by role`() {
        val role = Role.USER
        val roleUsers = listOf(UserBuilder.build(role = role).toResponse())
        `when`(userService.findByRole(role)).thenReturn(roleUsers)

        val result = mockMvc.perform(get("/users").queryParam("role", role.name))
                .andExpect(status().isOk)
                .andReturn()

        assertEquals(result.response.contentAsString, jacksonObjectMapper().writeValueAsString(roleUsers))
    }

    @Test
    fun `getById existing user`() {
        val existingId = "existingId"
        val existingUser = UserBuilder.build(role = Role.USER).toResponse()
        `when`(userService.findById(existingId)).thenReturn(existingUser)

        val result = mockMvc.perform(get("/users/$existingId"))
                .andExpect(status().isOk)
                .andReturn()

        assertEquals(result.response.contentAsString, jacksonObjectMapper().writeValueAsString(existingUser))
    }

    @Test
    fun `getById not found`() {
        val notExistingId = "notExistingId"
        `when`(userService.findById(notExistingId)).thenThrow(
            TripecoException.Validation(HttpStatus.NOT_FOUND,
                "error message"))

        mockMvc.perform(get("/users/$notExistingId")).andExpect(status().isNotFound)
    }

    @Test
    fun `delete existing user`() {
        val existingId = "existingId"
        mockMvc.perform(delete("/users/$existingId"))
                .andExpect(status().isNoContent)
    }

    @Test
    fun `update existing user`() {
        val existingId = "existingId"
        val userUpdateDto = UpdateUserRequest("firstName", "lastName", "phoneCode", "address",
                "zipCode", Role.USER)
        mockMvc.perform(patch("/users/$existingId")
                .contentType("application/json")
                .content(jacksonObjectMapper().writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk)
    }

    @Test
    fun `update non-existing user`() {
        val nonExistingId = "nonExistingId"
        val userUpdateDto = UpdateUserRequest("firstName", "lastName", "phoneCode", "address",
                "zipCode", Role.USER )
        `when`(userService.update(nonExistingId, userUpdateDto))
                .thenThrow(TripecoException.Validation(HttpStatus.NOT_FOUND, "error message"))
        mockMvc.perform(patch("/users/$nonExistingId")
                .contentType("application/json")
                .content(jacksonObjectMapper().writeValueAsString(userUpdateDto)))
                .andExpect(status().isNotFound)
    }

    @Test
    fun `resend password`() {
        val existingId = "existingId"
        mockMvc.perform(patch("/users/$existingId/password"))
                .andExpect(status().isNoContent)
    }

}
