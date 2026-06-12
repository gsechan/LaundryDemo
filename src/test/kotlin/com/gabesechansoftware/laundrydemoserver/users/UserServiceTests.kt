package com.gabesechansoftware.laundrydemoserver.users

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomerFacing
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrganizationRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.UserRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.customerview.Address as CustomerAddress
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.validation.AddressValidator
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class UserServiceTests {

    @MockK
    private lateinit var userRepository: UserRepository
    @MockK
    private lateinit var loginAuthenticator: LoginAuthenticator
    @MockK
    private lateinit var organizationRepository: OrganizationRepository
    @MockK
    private lateinit var addressRepository: AddressRepository

    @InjectMockKs
    private lateinit var userService: UserService

    private val organization = Organization("Laundry", "en-US")
    private val user = User("Gabe","test@example.com","3128675309",organization,mutableListOf())
    private val address = CustomerAddress("1","street1", "street2", "city", "state", "country", "code")

    @BeforeTest
    fun setUp() {
        user.addresses.clear()
    }

    @Test
    fun `addAddress-  valid and no pre-existing addresses, adds as default`() {
        every { addressRepository.countAddressesByUser(any()) } returns 0
        every { addressRepository.save(any()) } returnsArgument 0

        val result = userService.addAddress(user, address)

        verify { addressRepository.save(result) }
        assertEquals(address.street1, result.street1)
        assertEquals(address.street2, result.street2)
        assertEquals(address.city, result.city)
        assertEquals(address.state, result.state)
        assertEquals(address.country, result.country)
        assertEquals(address.postcode, result.postcode)
        assertTrue( result.isDefault!!)
        assertSize(1, user.addresses)
        assertEquals(result, user.addresses[0])
    }

    @Test
    fun `addAddress-  valid with a pre-existing addresses, adds non-default`() {
        every { addressRepository.countAddressesByUser(any()) } returns 1
        every { addressRepository.save(any()) } returnsArgument 0

        val result = userService.addAddress(user, address)

        verify { addressRepository.save(result) }
        assertEquals(address.street1, result.street1)
        assertEquals(address.street2, result.street2)
        assertEquals(address.city, result.city)
        assertEquals(address.state, result.state)
        assertEquals(address.country, result.country)
        assertEquals(address.postcode, result.postcode)
        assertFalse( result.isDefault!!)
        assertSize(1, user.addresses)
        assertEquals(result, user.addresses[0])
    }

    @Test
    fun `addAddress-  valid with too many addresses, throws exception`() {
        every { addressRepository.countAddressesByUser(any()) } returns 5
        every { addressRepository.save(any()) } returnsArgument 0
        repeat(5) {user.addresses.add(Address())}

        assertThrows<APIErrorException> { userService.addAddress(user, address) }
        verify (exactly = 0){ addressRepository.save(any()) }
    }

    @Test
    fun `addAddress-  validator failure throws exception`() {
        val mockValidator = mockk<AddressValidator>()
        val service = UserService(userRepository, loginAuthenticator, organizationRepository, addressRepository, mockValidator)
        every { addressRepository.countAddressesByUser(any()) } returns 0
        every { addressRepository.save(any()) } returnsArgument 0
        every { mockValidator.validateCustomerAddress(any(), any()) } answers { (args[1] as MutableList<String>).add("Error") }

        assertThrows<APIErrorException> { service.addAddress(user, address) }
        verify (exactly = 0){ addressRepository.save(any()) }

    }

    @Test
    fun `createUser-  invalid password throws exception`() {
        every { userRepository.save(any()) } returnsArgument 0

        assertThrows<APIErrorException> { userService.createUser(user.toCustomerFacing(), "1234", organization.id) }
        verify (exactly = 0){ userRepository.save(any()) }
    }

    @Test
    fun `createUser-  invalid user throws exception`() {
        every { userRepository.save(any()) } returnsArgument 0
        val badUser = User("Gabe","test@example.com","31",organization,mutableListOf())
        assertThrows<APIErrorException> { userService.createUser(badUser.toCustomerFacing(), "12345678", organization.id) }
        verify (exactly = 0){ userRepository.save(any()) }
    }

    @Test
    fun `createUser-  too many adddresses throws exception`() {
        every { userRepository.save(any()) } returnsArgument 0
        repeat(6) {user.addresses.add(Address("a","b","c","d","e","f", false))}
        assertThrows<APIErrorException> { userService.createUser(user.toCustomerFacing(), "12345678", organization.id) }
        verify (exactly = 0){ userRepository.save(any()) }
    }

    @Test
    fun `createUser-  valid user passes`() {
        every { userRepository.save(any()) } returnsArgument 0
        every { organizationRepository.getReferenceById(any()) } returns organization
        every { loginAuthenticator.setPasswordForUser(any(), any()) } just Runs
        val result =  userService.createUser(user.toCustomerFacing(), "12345678", organization.id)
        verify { userRepository.save(result) }
        verify { loginAuthenticator.setPasswordForUser(user, "12345678") }
    }

}
