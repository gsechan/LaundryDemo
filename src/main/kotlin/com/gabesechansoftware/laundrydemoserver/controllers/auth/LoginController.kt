package com.gabesechansoftware.laundrydemoserver.controllers.auth

import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.user.Address
import com.gabesechansoftware.laundrydemoserver.model.user.User
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
    val addresses: List<LoginAddress>
)

data class LoginAddress(
    val street1: String,
    val street2: String?,
    val city: String,
    val state: String,
    val country: String,
    val postcode: String
)

data class CheckAuthRequest(val token: String)
data class CheckAuthResponse(val success: Boolean)


@RestController
class LoginController(
    @Autowired private val loginAuthenticator: LoginAuthenticator
) {

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

    fun User.toLoginUser(): LoginUser {
        val sorted = this.addresses?.sortedBy { if (it.isDefault!!) 0 else 1 } ?:emptyList()

        return LoginUser(name!!, email, phone!!, sorted.map{it.toLoginAddress()})
    }

    fun Address.toLoginAddress(): LoginAddress {
        return LoginAddress(street1!!, street2, city!!, state!!, country!!, postcode!!)
    }

    @PostMapping("/checkAuth")
    fun checkAuth(
        @RequestBody request: CheckAuthRequest): CheckAuthResponse {
            try {
                loginAuthenticator.authenticateToken(request.token)
                return CheckAuthResponse(true)
            }
            catch (ex: Exception) {
                ex.printStackTrace()
                return CheckAuthResponse(false)
            }
    }
}