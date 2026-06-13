package com.gabesechansoftware.laundrydemoserver.users

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadUser
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrganizationRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.UserRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.validation.AddressValidator
import com.gabesechansoftware.laundrydemoserver.model.validation.UserValidator
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
    private val uploadUser = UploadUser("Gabe","test@example.com","3128675309",mutableListOf())
    private val address = UploadAddress("street1", "street2", "city", "state", "country", "code")

    @BeforeTest
    fun setUp() {
        user.addresses.clear()
    }

    @Test
    fun `addAddress-  valid and no pre-existing addresses, adds as default`() {
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
        every { addressRepository.save(any()) } returnsArgument 0
        user.addresses.add(address.toDBAddress(null, true))
        val result = userService.addAddress(user, address)

        verify { addressRepository.save(result) }
        assertEquals(address.street1, result.street1)
        assertEquals(address.street2, result.street2)
        assertEquals(address.city, result.city)
        assertEquals(address.state, result.state)
        assertEquals(address.country, result.country)
        assertEquals(address.postcode, result.postcode)
        assertFalse( result.isDefault!!)
        assertSize(2, user.addresses)
        assertEquals(result, user.addresses[1])
    }

    @Test
    fun `addAddress-  valid with too many addresses, throws exception`() {
        every { addressRepository.save(any()) } returnsArgument 0
        repeat(5) {user.addresses.add(Address())}

        assertThrows<APIErrorException> { userService.addAddress(user, address) }
        verify (exactly = 0){ addressRepository.save(any()) }
    }

    @Test
    fun `addAddress-  validator failure throws exception`() {
        val mockValidator = mockk<AddressValidator>()
        val service = UserService(userRepository, loginAuthenticator, organizationRepository, addressRepository, mockValidator)
        every { addressRepository.save(any()) } returnsArgument 0
        every { mockValidator.validateAddress(any(), any()) } answers { (args[1] as MutableList<String>).add("Error") }

        assertThrows<APIErrorException> { service.addAddress(user, address) }
        verify (exactly = 0){ addressRepository.save(any()) }

    }

    @Test
    fun `createUser-  invalid user throws exception`() {
        every { userRepository.save(any()) } returnsArgument 0
        every { organizationRepository.getReferenceById(any()) } returns organization
        val badUser = UploadUser("Gabe","test@example.com","31",mutableListOf())
        assertThrows<APIErrorException> { userService.createUser(badUser, "12345678", organization.id) }
        verify (exactly = 0){ userRepository.save(any()) }
    }

    @Test
    fun `createUser-  too many addresses throws exception`() {
        every { userRepository.save(any()) } returnsArgument 0
        every { organizationRepository.getReferenceById(any()) } returns organization
        val address =  UploadAddress("a","b","c","d","e","f")
        val addresses = listOf(address, address, address, address, address, address)
        assertThrows<APIErrorException> { userService.createUser(uploadUser.copy(addresses = addresses), "12345678", organization.id) }
        verify (exactly = 0){ userRepository.save(any()) }
    }

    @Test
    fun `createUser-  valid user passes`() {
        every { userRepository.save(any()) } returnsArgument 0
        every { organizationRepository.getReferenceById(any()) } returns organization
        every { loginAuthenticator.createPasswordForUser(any(), any()) } just Runs
        val result =  userService.createUser(uploadUser, "12345678", organization.id)
        verify { userRepository.save(result) }
        verify { loginAuthenticator.createPasswordForUser(user, "12345678") }
    }

    @Test
    fun `updateUser-  invalid user throws exception`() {
        val validator = mockk<UserValidator>()
        every { validator.validateUser(any(), any()) } answers {(args[1] as MutableList<String>).add("Error")}

        val service = UserService(userRepository, loginAuthenticator, organizationRepository, addressRepository, userValidator = validator)

        assertThrows<APIErrorException> {
            service.updateUser(user, "Me", "me@me.com", "3128675309", "newpassword")
        }
    }

    @Test
    fun `updateUser-  name changes are saved`() {
        every { userRepository.save(any()) } returnsArgument 0
        val result = userService.updateUser(user, "Me", null, null, null)
        assertEquals("Me", result.name)
        assertEquals("test@example.com", result.email)
        assertEquals("3128675309", result.phone)

        verify { userRepository.save(result) }
        verify(exactly = 0){ loginAuthenticator.updatePasswordForUser(any(), any()) }
    }

    @Test
    fun `updateUser-  email changes are saved`() {
        every { userRepository.save(any()) } returnsArgument 0
        val result = userService.updateUser(user, null, "me@example.com", null, null)
        assertEquals("Gabe", result.name)
        assertEquals("me@example.com", result.email)
        assertEquals("3128675309", result.phone)

        verify { userRepository.save(result) }
        verify(exactly = 0){ loginAuthenticator.updatePasswordForUser(any(), any()) }
    }

    @Test
    fun `updateUser-  phone changes are saved`() {
        every { userRepository.save(any()) } returnsArgument 0
        val result = userService.updateUser(user, null, null, "3125882300", null)
        assertEquals("Gabe", result.name)
        assertEquals("test@example.com", result.email)
        assertEquals("3125882300", result.phone)

        verify { userRepository.save(result) }
        verify(exactly = 0){ loginAuthenticator.updatePasswordForUser(any(), any()) }
    }

    @Test
    fun `updateUser-  password changes are saved`() {
        every { userRepository.save(any()) } returnsArgument 0
        every { loginAuthenticator.updatePasswordForUser(any(), any()) } just Runs
        val result = userService.updateUser(user, null, null, null, "newpassword")
        assertEquals("Gabe", result.name)
        assertEquals("test@example.com", result.email)
        assertEquals("3128675309", result.phone)

        verify { userRepository.save(result) }
        verify(exactly = 1){ loginAuthenticator.updatePasswordForUser(user, "newpassword") }
    }

}
