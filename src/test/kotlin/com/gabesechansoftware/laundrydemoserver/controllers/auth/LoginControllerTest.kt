package com.gabesechansoftware.laundrydemoserver.controllers.auth

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.assertEmpty
import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.auth.BadLoginException
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Session
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class LoginControllerTest {
    @MockK
    private lateinit var authenticator: LoginAuthenticator

    @InjectMockKs
    private lateinit var controller: LoginController

    val street1 = "211 Broad"
    val street2 = "Suite 2"
    val city = "Chicago"
    val state = "Illinois"
    val country = "USA"
    val postCode = "10022"
    val address = Address(street1, street2, city, state, country, postCode)

    val name = "Gabe"
    val email = "test@example.com"
    val phone = "3128675309"
    val organization = Organization("My Laundry", "en-US")
    val user = User(name, email, phone, organization, mutableListOf(address))

    val token = "token"
    val expiration = OffsetDateTime.now()
    val session = Session(user, token, expiration)

    @Test
    fun `login-  if authenticator throws, return an error`() {
        every { authenticator.authenticatePassword(any(), any(), any()) } throws BadLoginException()
        val request = LoginRequest("phone", "password", "org")

        val response = controller.login(request)
        assertEquals(NetworkErrorType.BAD_AUTH.toString(), response.errorType)
        assertNotEmpty(response.errors)
        assertNull(response.data)
    }

    @Test
    fun `login-  if authenticator returns a value, respond with it`() {
        every { authenticator.authenticatePassword(any(), any(), any()) } returns user
        every { authenticator.createSession(any()) } returns session
        val request = LoginRequest("3128675309", "password", organization.id.toString())

        val response = controller.login(request)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertEmpty(response.errors)
        assertNotNull(response.data)
        assertEquals(token, response.data.session)
        assertEquals(name, response.data.user.name)
        assertEquals(email, response.data.user.email)
        assertEquals(phone, response.data.user.phone)
        assertSize(1, response.data.user.addresses)
        val resAddress = response.data.user.addresses[0]
        assertEquals(street1, resAddress.street1)
        assertEquals(street2, resAddress.street2)
        assertEquals(state, resAddress.state)
        assertEquals(country, resAddress.country)
        assertEquals(city, resAddress.city)
        assertEquals(postCode, resAddress.postcode)
    }


    @Test
    fun `logout-  if authenticator throws, return an error`() {
        every { authenticator.logout(token) } throws BadLoginException()
        val request = LoginRequest("phone", "password", "org")

        val response = controller.logout(token)
        assertEquals(NetworkErrorType.BAD_AUTH.toString(), response.errorType)
        assertNotEmpty(response.errors)
        assertNull(response.data)
    }

    @Test
    fun `logout-  if authenticator succeeds, return Success`() {
        every { authenticator.logout(token) } returns Unit
        val request = LoginRequest("phone", "password", "org")

        val response = controller.logout(token)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertEmpty(response.errors)
        assertNotNull(response.data)
        assertEquals(Unit, response.data)
    }


    @Test
    fun `checkAuth-  if authenticator throws, return an error`() {
        every { authenticator.authenticateToken(any()) } throws BadLoginException()
        val request = CheckAuthRequest(token)

        val response = controller.checkAuth(request)
        assertEquals(NetworkErrorType.BAD_AUTH.toString(), response.errorType)
        assertNotEmpty(response.errors)
        assertNull(response.data)
    }

    @Test
    fun `checkAuth-  if authenticator returns a value, respond with it`() {
        every { authenticator.authenticateToken(token) } returns user
        val request = CheckAuthRequest(token)

        val response = controller.checkAuth(request)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertEmpty(response.errors)
        assertNotNull(response.data)
        assertEquals(name, response.data.name)
        assertEquals(email, response.data.email)
        assertEquals(phone, response.data.phone)
        assertSize(1, response.data.addresses)
        val resAddress = response.data.addresses[0]
        assertEquals(street1, resAddress.street1)
        assertEquals(street2, resAddress.street2)
        assertEquals(state, resAddress.state)
        assertEquals(country, resAddress.country)
        assertEquals(city, resAddress.city)
        assertEquals(postCode, resAddress.postcode)
    }
}