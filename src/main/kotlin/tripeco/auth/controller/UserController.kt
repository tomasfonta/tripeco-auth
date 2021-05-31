package tripeco.auth.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import tripeco.auth.model.Role
import tripeco.auth.service.ChangePasswordRequest
import tripeco.auth.service.RegisterUserRequest
import tripeco.auth.service.UpdateUserRequest
import tripeco.auth.service.UserService

@RestController
@RequestMapping("users")
class UserController(private val userService: UserService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody userRequest: RegisterUserRequest) = userService.register(userRequest)

    @GetMapping
    fun getAll(@RequestParam("role") role: Role?) =
            if (role != null) {
                userService.findByRole(role)
            } else {
                userService.findAll()
            }

    @GetMapping("{id}")
    fun getById(@PathVariable("id") id: String) = userService.findById(id)

    @GetMapping("current")
    fun getCurrent() = userService.getCurrentResponse()

    @GetMapping("me")
    fun getMyProfile() = userService.getCurrent()

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable("id") id: String) {
        userService.delete(id)
    }

    @PatchMapping("{id}")
    fun update(@PathVariable("id") id: String, @RequestBody updateUserRequest: UpdateUserRequest) =
        userService.update(id, updateUserRequest)

    @PatchMapping("password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changePassword(@RequestBody request: ChangePasswordRequest) {
        userService.changePassword(request)
    }

    @PatchMapping("{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun resendPassword(@PathVariable("id") id: String) {
        userService.resendPassword(id)
    }

}
