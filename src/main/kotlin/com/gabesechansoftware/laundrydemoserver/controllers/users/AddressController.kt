package com.gabesechansoftware.laundrydemoserver.controllers.users

import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.authentication.AuthenticatedUser
import com.gabesechansoftware.laundrydemoserver.model.customerview.PatchAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomer
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.users.UserService
import com.gabesechansoftware.laundrydemoserver.model.customerview.Address as CustomerAddress
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class PostAddressRequest(val address: UploadAddress)
data class PostAddressResponse(val address: CustomerAddress)
data class PatchAddressRequest(val address: PatchAddress)
data class PatchAddressResponse(val address: CustomerAddress)


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

    @PatchMapping("/addresses/{id}")
    fun updateAddress(
        @PathVariable id: UUID,
        @RequestBody request: PatchAddressRequest,
        @AuthenticatedUser authedUser: User,
    ): NetworkResponse<PatchAddressResponse> {
        val result = userService.updateAddress(authedUser, id, request.address)
        return NetworkResponse(PatchAddressResponse(result.toCustomer()))
    }

    @DeleteMapping("/addresses/{id}")
    fun deleteAddress(
        @PathVariable id: UUID,
        @AuthenticatedUser authedUser: User,
    ): NetworkResponse<Unit> {
        userService.deleteAddress(authedUser, id)
        return NetworkResponse(Unit)
    }
}