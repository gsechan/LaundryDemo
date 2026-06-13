package com.gabesechansoftware.laundrydemoserver.controllers.users

import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.auth.AuthenticatedUser
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.customerview.PatchUser
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadUser
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomer
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.users.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import com.gabesechansoftware.laundrydemoserver.model.customerview.User as CustomerUser
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class CreateUserRequest(
    val user: UploadUser,
    val password: String,
    val org: String,
)

data class CreateUserResponse(
    val session: String,
    val user: CustomerUser
)

data class UpdateUserRequest(
    val user: PatchUser,
)

@RestController
class UserController(
    private val userService: UserService,
    private val loginAuthenticator: LoginAuthenticator
) {

    @PostMapping("/users")
    fun createUser(@RequestBody request: CreateUserRequest): NetworkResponse<CreateUserResponse> {
        val orgId = UUID.fromString(request.org)
        val user = request.user
        val password = request.password
        val resultUser = userService.createUser(user, password, orgId)
        val session = loginAuthenticator.createSession(resultUser)
        return NetworkResponse(CreateUserResponse(session.token!!, resultUser.toCustomer()))
    }

    @GetMapping("/users/me")
    fun getLoggedInUser(@AuthenticatedUser user: User): NetworkResponse<CustomerUser> {
        return NetworkResponse(user.toCustomer())
    }

    @PatchMapping("/users/me")
    fun updateLoggedInUser(@RequestBody request: UpdateUserRequest, @AuthenticatedUser user: User): NetworkResponse<CustomerUser> {
        userService.updateUser(user, request.user.name, request.user.email, request.user.phone, request.user.password)
        return NetworkResponse(user.toCustomer())
    }

}