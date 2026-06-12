package com.gabesechansoftware.laundrydemoserver.controllers.users

import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomer
import com.gabesechansoftware.laundrydemoserver.users.UserService
import com.gabesechansoftware.laundrydemoserver.model.customerview.User as CustomerUser
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class CreateUserRequest(
    val user: CustomerUser,
    val password: String,
    val org: String,
)

data class CreateUserResponse(
    val session: String,
    val user: CustomerUser
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

}