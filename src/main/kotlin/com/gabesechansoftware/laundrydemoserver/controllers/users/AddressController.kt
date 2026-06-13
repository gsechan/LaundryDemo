package com.gabesechansoftware.laundrydemoserver.controllers.users

import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.auth.AuthenticatedUser
import com.gabesechansoftware.laundrydemoserver.model.customerview.Address
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomer
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.users.UserService
import com.gabesechansoftware.laundrydemoserver.model.customerview.Address as CustomerAddress
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class PostAddressRequest(val address: UploadAddress)
data class PostAddressResponse(val address: CustomerAddress)


@RestController
class AddressController(private val userService: UserService) {

    @PostMapping("/addresses")
    fun addAddress(
        @RequestBody request: PostAddressRequest,
        @AuthenticatedUser authedUser: User,
    ) : NetworkResponse<PostAddressResponse> {
        val result = userService.addAddress(authedUser, request.address)
        return NetworkResponse(PostAddressResponse(result.toCustomer()))
    }
}