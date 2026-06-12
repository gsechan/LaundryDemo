package com.gabesechansoftware.laundrydemoserver.controllers.users

import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.auth.AuthenticatedUser
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomer
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.users.UserService
import com.gabesechansoftware.laundrydemoserver.model.customerview.Address as CustomerAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.User as CustomerUser
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class PostAddressRequest(val address: CustomerAddress)
data class PostAddressResponse(val user:CustomerUser)


@RestController
class AddressController(private val userService: UserService) {

    @PostMapping("/addresses")
    fun addAddress(
        @RequestBody request: PostAddressRequest,
        @AuthenticatedUser authedUser: User,
    ) : NetworkResponse<PostAddressResponse> {
        userService.addAddress(authedUser, request.address)
        val user = authedUser.toCustomer()
        return NetworkResponse(PostAddressResponse(user))
    }
}