package com.gabesechansoftware.laundrydemoserver

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class LoginRequest(val username: String, val password: String)

data class LoginResponse(
    val success: Boolean,
    val user: LoginUser
)

data class LoginUser(
    val id: String,
    val name: String,
    val email: String?,
    val phone: String?
)

@RestController
class LoginController {
    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest): LoginResponse {
        return LoginResponse(true, LoginUser("1","Gabe", "",""))
    }
}