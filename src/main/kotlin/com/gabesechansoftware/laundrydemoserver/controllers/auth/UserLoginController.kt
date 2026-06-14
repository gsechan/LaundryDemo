package com.gabesechansoftware.laundrydemoserver.controllers.auth

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.auth.UserLoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomer
import com.gabesechansoftware.laundrydemoserver.model.customerview.User as CustomerUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.UUID



data class LoginRequest(val phone: String, val password: String, val organization: String)

data class LoginResponse(
    val session: String,
    val user: CustomerUser
)


data class CheckAuthRequest(val token: String)


@RestController
class LoginController(
    private val userLoginAuthenticator: UserLoginAuthenticator
) {

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest): NetworkResponse<LoginResponse> {
        try {
            val user = userLoginAuthenticator.authenticatePassword(
                UUID.fromString(request.organization),
                request.phone,
                request.password
            )
            val session = userLoginAuthenticator.createSession(user)
            return NetworkResponse(LoginResponse(session.token!!, session.user!!.toCustomer()))
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            return NetworkResponse(NetworkErrorType.BAD_AUTH, "Could not authenticate")
        }
    }


    @GetMapping("/logout")
    fun logout(@RequestHeader("Authorization") authHeader: String ): NetworkResponse<Unit> {
        //Can't use authenticated user because we need the actual token
        try {
            val token = authHeader.removePrefix("Bearer ")
            userLoginAuthenticator.logout(token)
        }
        catch (e: Exception) {
            e.printStackTrace()
            return NetworkResponse(NetworkErrorType.BAD_AUTH, "Token invalid")
        }
        return NetworkResponse(Unit)
    }

    @PostMapping("/checkAuth")
    fun checkAuth(
        @RequestBody request: CheckAuthRequest): NetworkResponse<CustomerUser> {
            try {
                val user = userLoginAuthenticator.authenticateToken(request.token)
                return NetworkResponse(  user.toCustomer())
            }
            catch (ex: Exception) {
                ex.printStackTrace()
                return NetworkResponse(NetworkErrorType.BAD_AUTH, "Invalid session token")
            }
    }
}