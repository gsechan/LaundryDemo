package com.gabesechansoftware.laundrydemoserver.controllers.auth

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomerFacing
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
    private val loginAuthenticator: LoginAuthenticator
) {

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest): NetworkResponse<LoginResponse> {
        try {
            val session = loginAuthenticator.authenticateLoginAndCreateSession(
                UUID.fromString(request.organization),
                request.phone,
                request.password
            )
            return NetworkResponse(LoginResponse(session.token, session.user.toCustomerFacing()))
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            return NetworkResponse(NetworkErrorType.BAD_AUTH, "Could not authenticate")
        }
    }

    @GetMapping("/logout")
    fun logout(@RequestHeader("Authorization") authHeader: String ): NetworkResponse<Unit> {
        try {
            val token = authHeader.substringAfter(" ")
            loginAuthenticator.logout(token)
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
                val user = loginAuthenticator.authenticateToken(request.token)
                return NetworkResponse(  user.toCustomerFacing())
            }
            catch (ex: Exception) {
                ex.printStackTrace()
                return NetworkResponse(NetworkErrorType.BAD_AUTH, "Invalid session token")
            }
    }
}