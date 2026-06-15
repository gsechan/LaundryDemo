package com.gabesechansoftware.laundrydemoserver.controllers.auth

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.admins.AdminView
import com.gabesechansoftware.laundrydemoserver.admins.AdminViewMapper
import com.gabesechansoftware.laundrydemoserver.authentication.AdminLoginAuthenticator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController


data class AdminLoginRequest(val email: String, val password: String)

data class AdminLoginResponse(
    val session: String,
    val admin: AdminView
)

data class AdminCheckAuthRequest(val token: String)


@RestController
class AdminLoginController(
    private val adminLoginAuthenticator: AdminLoginAuthenticator,
    private val adminViewMapper: AdminViewMapper,
) {

    @PostMapping("/admin/login")
    fun login(
        @RequestBody request: AdminLoginRequest): NetworkResponse<AdminLoginResponse> {
        try {
            val admin = adminLoginAuthenticator.authenticatePassword(
                request.email,
                request.password
            )
            val session = adminLoginAuthenticator.createSession(admin)
            return NetworkResponse(AdminLoginResponse(session.token!!, adminViewMapper.toView(session.admin!!)))
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            return NetworkResponse(NetworkErrorType.BAD_AUTH, "Could not authenticate")
        }
    }


    @GetMapping("/admin/logout")
    fun logout(@RequestHeader("Authorization") authHeader: String ): NetworkResponse<Unit> {
        //Can't use authenticated admin because we need the actual token
        try {
            val token = authHeader.removePrefix("Bearer ")
            adminLoginAuthenticator.logout(token)
        }
        catch (e: Exception) {
            e.printStackTrace()
            return NetworkResponse(NetworkErrorType.BAD_AUTH, "Token invalid")
        }
        return NetworkResponse(Unit)
    }

    @PostMapping("/admin/checkAuth")
    fun checkAuth(
        @RequestBody request: AdminCheckAuthRequest): NetworkResponse<AdminView> {
            try {
                val admin = adminLoginAuthenticator.authenticateToken(request.token)
                return NetworkResponse(adminViewMapper.toView(admin))
            }
            catch (ex: Exception) {
                ex.printStackTrace()
                return NetworkResponse(NetworkErrorType.BAD_AUTH, "Invalid session token")
            }
    }
}
