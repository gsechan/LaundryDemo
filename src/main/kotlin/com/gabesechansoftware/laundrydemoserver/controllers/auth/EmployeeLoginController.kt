package com.gabesechansoftware.laundrydemoserver.controllers.auth

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.authentication.EmployeeLoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.employees.EmployeeView
import com.gabesechansoftware.laundrydemoserver.employees.EmployeeViewMapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

data class EmployeeLoginRequest(val phone: String, val password: String)

data class EmployeeLoginResponse(
    val session: String,
    val employee: EmployeeView,
)

data class EmployeeCheckAuthRequest(val token: String)

@RestController
class EmployeeLoginController(
    private val employeeLoginAuthenticator: EmployeeLoginAuthenticator,
    private val employeeViewMapper: EmployeeViewMapper,
) {

    @PostMapping("/employee/login")
    fun login(@RequestBody request: EmployeeLoginRequest): NetworkResponse<EmployeeLoginResponse> {
        try {
            val employee = employeeLoginAuthenticator.authenticatePassword(request.phone, request.password)
            val session = employeeLoginAuthenticator.createSession(employee)
            return NetworkResponse(EmployeeLoginResponse(session.token!!, employeeViewMapper.toView(session.employee!!)))
        } catch (ex: Exception) {
            ex.printStackTrace()
            return NetworkResponse(NetworkErrorType.BAD_AUTH, "Could not authenticate")
        }
    }

    @GetMapping("/employee/logout")
    fun logout(@RequestHeader("Authorization") authHeader: String): NetworkResponse<Unit> {
        try {
            val token = authHeader.removePrefix("Bearer ")
            employeeLoginAuthenticator.logout(token)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return NetworkResponse(NetworkErrorType.BAD_AUTH, "Token invalid")
        }
        return NetworkResponse(Unit)
    }

    @PostMapping("/employee/checkAuth")
    fun checkAuth(@RequestBody request: EmployeeCheckAuthRequest): NetworkResponse<EmployeeView> {
        try {
            val employee = employeeLoginAuthenticator.authenticateToken(request.token)
            return NetworkResponse(employeeViewMapper.toView(employee))
        } catch (ex: Exception) {
            ex.printStackTrace()
            return NetworkResponse(NetworkErrorType.BAD_AUTH, "Invalid session token")
        }
    }
}
