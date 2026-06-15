package com.gabesechansoftware.laundrydemoserver.users

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.authentication.UserLoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.customerview.PatchAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.PatchUser
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadUser
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrganizationRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.customer.UserRepository
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
import java.util.UUID
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
    private lateinit var userLoginAuthenticator: UserLoginAuthenticator
    @MockK
    private lateinit var organizationRepository: OrganizationRepository
    @MockK
    private lateinit var addressRepository: AddressRepository

    @InjectMockKs
    private lateinit var userService: UserService

    private val organization = Organization("Laundry", "en-US")
    private val user = User(name = "Gabe", email = "test@example.com", phone = "3128675309", organization = organization, addresses = mutableListOf())
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
        val service = UserService(
            userRepository = userRepository,
            userLoginAuthenticator = userLoginAuthenticator,
            organizationRepository = organizationRepository,
            addressRepository = addressRepository,
            addressValidator = mockValidator,
        )
        every { addressRepository.save(any()) } returnsArgument 0
        every { mockValidator.validateAddress(any(), any()) } answers { (args[1] as MutableList<String>).add("Error") }

        assertThrows<APIErrorException> { service.addAddress(user, address) }
        verify (exactly = 0){ addressRepository.save(any()) }

    }

    @Test
    fun `listByOrganization- returns users for the organization`() {
        val orgId = UUID.randomUUID()
        val users = listOf(
            User(name = "Gabe", email = "gabe@example.com", phone = "3128675309"),
            User(name = "Sue", email = "sue@example.com", phone = "2065551212"),
        )
        every { userRepository.findByOrganizationId(orgId) } returns users

        val result = userService.listByOrganization(orgId)

        assertSize(2, result)
        assertEquals(users, result)
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
        every { userLoginAuthenticator.createPasswordForUser(any(), any()) } just Runs
        val result =  userService.createUser(uploadUser, "12345678", organization.id)
        verify { userRepository.save(result) }
        verify { userLoginAuthenticator.createPasswordForUser(user, "12345678") }
    }

    @Test
    fun `deleteAddress-  address belongs to user, is removed and deleted`() {
        every { addressRepository.delete(any()) } just Runs
        user.addresses.add(address.toDBAddress(null, true))
        val dbAddress = user.addresses[0]

        userService.deleteAddress(user, dbAddress.id)

        assertSize(0, user.addresses)
        verify { addressRepository.delete(dbAddress) }
    }

    @Test
    fun `deleteAddress-  address does not belong to user, throws and nothing is deleted`() {
        user.addresses.add(address.toDBAddress(null, true))
        val otherAddressId = Address().id

        assertThrows<EntityDoesNotExistException> { userService.deleteAddress(user, otherAddressId) }

        assertSize(1, user.addresses)
        verify(exactly = 0) { addressRepository.delete(any()) }
    }

    @Test
    fun `updateAddress-  address belongs to user, fields are patched and saved`() {
        every { addressRepository.save(any()) } returnsArgument 0
        user.addresses.add(address.toDBAddress(null, true))
        val dbAddress = user.addresses[0]
        val patch = PatchAddress(
            street1 = "newStreet1",
            street2 = "newStreet2",
            city = "newCity",
            state = "newState",
            country = "newCountry",
            postcode = "newPostcode",
        )

        val result = userService.updateAddress(user, dbAddress.id, patch)

        assertEquals("newStreet1", result.street1)
        assertEquals("newStreet2", result.street2)
        assertEquals("newCity", result.city)
        assertEquals("newState", result.state)
        assertEquals("newCountry", result.country)
        assertEquals("newPostcode", result.postcode)
        verify { addressRepository.save(dbAddress) }
    }

    @Test
    fun `updateAddress-  null fields in patch leave existing values unchanged`() {
        every { addressRepository.save(any()) } returnsArgument 0
        user.addresses.add(address.toDBAddress(null, true))
        val dbAddress = user.addresses[0]
        val originalStreet1 = dbAddress.street1
        val patch = PatchAddress(street1 = null, street2 = null, city = "newCity", state = null, country = null, postcode = null)

        val result = userService.updateAddress(user, dbAddress.id, patch)

        assertEquals(originalStreet1, result.street1)
        assertEquals("newCity", result.city)
    }

    @Test
    fun `updateAddress-  address does not belong to user, throws and nothing is saved`() {
        user.addresses.add(address.toDBAddress(null, true))
        val otherAddressId = Address().id
        val patch = PatchAddress(street1 = "x", street2 = null, city = null, state = null, country = null, postcode = null)

        assertThrows<EntityDoesNotExistException> { userService.updateAddress(user, otherAddressId, patch) }
        verify(exactly = 0) { addressRepository.save(any()) }
    }

    @Test
    fun `updateAddress-  validator failure throws exception and nothing is saved`() {
        val mockValidator = mockk<AddressValidator>()
        val service = UserService(
            userRepository = userRepository,
            userLoginAuthenticator = userLoginAuthenticator,
            organizationRepository = organizationRepository,
            addressRepository = addressRepository,
            addressValidator = mockValidator,
        )
        every { mockValidator.validateAddress(any(), any()) } answers { (args[1] as MutableList<String>).add("Error") }
        user.addresses.add(address.toDBAddress(null, true))
        val dbAddress = user.addresses[0]
        val patch = PatchAddress(street1 = "x", street2 = null, city = null, state = null, country = null, postcode = null)

        assertThrows<APIErrorException> { service.updateAddress(user, dbAddress.id, patch) }
        verify(exactly = 0) { addressRepository.save(any()) }
    }

    @Test
    fun `updateUser-  invalid user throws exception`() {
        val validator = mockk<UserValidator>()
        every { validator.validateUser(any(), any()) } answers {(args[1] as MutableList<String>).add("Error")}

        val service = UserService(
            userRepository = userRepository,
            userLoginAuthenticator = userLoginAuthenticator,
            organizationRepository = organizationRepository,
            addressRepository = addressRepository,
            userValidator = validator,
        )

        assertThrows<APIErrorException> {
            service.updateUser(user = user, patch = PatchUser(name = "Me", email = "me@me.com", phone = "3128675309", password = "newpassword"))
        }
    }

    @Test
    fun `updateUser-  name changes are saved`() {
        every { userRepository.save(any()) } returnsArgument 0
        val result = userService.updateUser(user = user, patch = PatchUser(name = "Me", email = null, phone = null, password = null))
        assertEquals("Me", result.name)
        assertEquals("test@example.com", result.email)
        assertEquals("3128675309", result.phone)

        verify { userRepository.save(result) }
        verify(exactly = 0){ userLoginAuthenticator.updatePasswordForUser(any(), any()) }
    }

    @Test
    fun `updateUser-  email changes are saved`() {
        every { userRepository.save(any()) } returnsArgument 0
        val result = userService.updateUser(user = user, patch = PatchUser(name = null, email = "me@example.com", phone = null, password = null))
        assertEquals("Gabe", result.name)
        assertEquals("me@example.com", result.email)
        assertEquals("3128675309", result.phone)

        verify { userRepository.save(result) }
        verify(exactly = 0){ userLoginAuthenticator.updatePasswordForUser(any(), any()) }
    }

    @Test
    fun `updateUser-  phone changes are saved`() {
        every { userRepository.save(any()) } returnsArgument 0
        val result = userService.updateUser(user = user, patch = PatchUser(name = null, email = null, phone = "3125882300", password = null))
        assertEquals("Gabe", result.name)
        assertEquals("test@example.com", result.email)
        assertEquals("3125882300", result.phone)

        verify { userRepository.save(result) }
        verify(exactly = 0){ userLoginAuthenticator.updatePasswordForUser(any(), any()) }
    }

    @Test
    fun `updateUser-  password changes are saved`() {
        every { userRepository.save(any()) } returnsArgument 0
        every { userLoginAuthenticator.updatePasswordForUser(any(), any()) } just Runs
        val result = userService.updateUser(user = user, patch = PatchUser(name = null, email = null, phone = null, password = "newpassword"))
        assertEquals("Gabe", result.name)
        assertEquals("test@example.com", result.email)
        assertEquals("3128675309", result.phone)

        verify { userRepository.save(result) }
        verify(exactly = 1){ userLoginAuthenticator.updatePasswordForUser(user, "newpassword") }
    }

}
