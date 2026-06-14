package com.gabesechansoftware.laundrydemoserver.controllers.users

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.controllers.auth.LoginController
import com.gabesechansoftware.laundrydemoserver.model.customerview.PatchAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomer
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.users.UserService
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AddressControllerTest {
    @MockK
    private lateinit var userService: UserService

    @InjectMockKs
    private lateinit var controller: AddressController

    @Test
    fun `addAddress returns a converted version of the returned value`() {
        val org = Organization()
        val uploadAddress = UploadAddress("s1","s2", "city", "state", "country", "postcode")
        val address = Address(street1 = "s1", street2 = "s2", city = "city", state = "state", country = "country", postcode = "postcode")
        val user = User(name = "Gabe", email = "test@example.com", phone = "3128675309", organization = org, addresses = mutableListOf())
        every { userService.addAddress(any(),any()) } returns address

        val request = PostAddressRequest(uploadAddress)
        val result = controller.addAddress(request, user)

        assertEquals(NetworkErrorType.NONE.toString(), result.errorType)
        assertEquals(address.toCustomer(), result.data!!.address)
    }

    @Test
    fun `updateAddress returns a converted version of the returned value`() {
        val org = Organization()
        val patchAddress = PatchAddress("s1", "s2", "city", "state", "country", "postcode")
        val address = Address(street1 = "s1", street2 = "s2", city = "city", state = "state", country = "country", postcode = "postcode")
        val user = User(name = "Gabe", email = "test@example.com", phone = "3128675309", organization = org, addresses = mutableListOf())
        val addressId = UUID.randomUUID()
        every { userService.updateAddress(any(), any(), any()) } returns address

        val request = PatchAddressRequest(patchAddress)
        val result = controller.updateAddress(addressId, request, user)

        assertEquals(NetworkErrorType.NONE.toString(), result.errorType)
        assertEquals(address.toCustomer(), result.data!!.address)
        verify { userService.updateAddress(user, addressId, patchAddress) }
    }

    @Test
    fun `deleteAddress delegates to userService with the authed user and address id`() {
        val org = Organization()
        val user = User(name = "Gabe", email = "test@example.com", phone = "3128675309", organization = org, addresses = mutableListOf())
        val addressId = UUID.randomUUID()
        every { userService.deleteAddress(any(), any()) } just Runs

        val result = controller.deleteAddress(addressId, user)

        assertEquals(NetworkErrorType.NONE.toString(), result.errorType)
        verify { userService.deleteAddress(user, addressId) }
    }
}