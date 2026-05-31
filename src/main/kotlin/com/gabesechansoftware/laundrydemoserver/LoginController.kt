package com.gabesechansoftware.laundrydemoserver

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class LoginRequest(val username: String, val password: String)

data class LoginResponse(
    val success: Boolean,
    val user: LoginUser?
)

data class LoginUser(
    val id: String,
    val name: String,
    val email: String?,
    val phone: String?,
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
    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest): LoginResponse {
        if(request.username == "Gabe" && request.password == "1234")
            return LoginResponse(
                true,
                LoginUser(
                    "1",
                    "Gabe",
                    "gsechan@hotmail.com",
                    "12067140469",
                    listOf(
                        Address("245 East 54th St #29D", null, "New York", "NY", "United States","10022"),
                        Address("435 Starwood Pass", "Suite 8", "Lake in the Hills", "IL", "United States","60156")
                    )
                )
            )
        else {
            return LoginResponse(false, null)
        }
    }
}