package com.gabesechansoftware.laundrydemoserver.controllers

import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class LoginRequest(val phone: String, val password: String, val organization: String)

data class LoginResponse(
    val success: Boolean,
    val session: String?,
    val user: LoginUser?
)

data class LoginUser(
    val name: String,
    val email: String?,
    val phone: String,
    val addresses: List<Address>
)

data class Address(
    val street1: String,
    val street2: String?,
    val city: String,
    val state: String,
    val country: String,
    val postcode: String
)

@RestController
class LoginController {
    @Autowired
    private lateinit var loginAuthenticator: LoginAuthenticator


    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest): LoginResponse {
        try {
            //TODO:  normalize phone number format
            val session = loginAuthenticator.authenticateLoginAndCreateSession(
                UUID.fromString(request.organization),
                request.phone,
                request.password
            )
            return LoginResponse(true, session.token, session.user.toLoginUser())
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            return LoginResponse(false, null, null)
        }
    }

    fun User.toLoginUser(): LoginUser = LoginUser(
        name!!, email, phone!!, emptyList()
    )
}