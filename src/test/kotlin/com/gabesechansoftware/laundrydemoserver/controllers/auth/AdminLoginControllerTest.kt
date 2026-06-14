package com.gabesechansoftware.laundrydemoserver.controllers.auth

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.assertEmpty
import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import com.gabesechansoftware.laundrydemoserver.authentication.AdminLoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.authentication.BadLoginException
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminSession
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
class AdminLoginControllerTest {
    @MockK
    private lateinit var authenticator: AdminLoginAuthenticator

    @InjectMockKs
    private lateinit var controller: AdminLoginController

    val name = "Gabe"
    val email = "admin@provider.com"
    val phone = "3128675309"
    val admin = Admin(name = name, email = email, phone = phone)

    val token = "token"
    val expiration = OffsetDateTime.now()!!
    val session = AdminSession(admin, token, expiration)

    @Test
    fun `login-  if authenticator throws, return an error`() {
        every { authenticator.authenticatePassword(any(), any()) } throws BadLoginException()
        val request = AdminLoginRequest("admin@provider.com", "password")

        val response = controller.login(request)
        assertEquals(NetworkErrorType.BAD_AUTH.toString(), response.errorType)
        assertNotEmpty(response.errors)
        assertNull(response.data)
    }

    @Test
    fun `login-  if authenticator returns a value, respond with it`() {
        every { authenticator.authenticatePassword(any(), any()) } returns admin
        every { authenticator.createSession(any()) } returns session
        val request = AdminLoginRequest(email, "password")

        val response = controller.login(request)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertEmpty(response.errors)
        assertNotNull(response.data)
        assertEquals(token, response.data.session)
        assertEquals(name, response.data.admin.name)
        assertEquals(email, response.data.admin.email)
        assertEquals(phone, response.data.admin.phone)
    }

    @Test
    fun `logout-  if authenticator throws, return an error`() {
        every { authenticator.logout(token) } throws BadLoginException()

        val response = controller.logout(token)
        assertEquals(NetworkErrorType.BAD_AUTH.toString(), response.errorType)
        assertNotEmpty(response.errors)
        assertNull(response.data)
    }

    @Test
    fun `logout-  if authenticator succeeds, return Success`() {
        every { authenticator.logout(token) } returns Unit

        val response = controller.logout(token)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertEmpty(response.errors)
        assertNotNull(response.data)
        assertEquals(Unit, response.data)
    }

    @Test
    fun `checkAuth-  if authenticator throws, return an error`() {
        every { authenticator.authenticateToken(any()) } throws BadLoginException()
        val request = AdminCheckAuthRequest(token)

        val response = controller.checkAuth(request)
        assertEquals(NetworkErrorType.BAD_AUTH.toString(), response.errorType)
        assertNotEmpty(response.errors)
        assertNull(response.data)
    }

    @Test
    fun `checkAuth-  if authenticator returns a value, respond with it`() {
        every { authenticator.authenticateToken(token) } returns admin
        val request = AdminCheckAuthRequest(token)

        val response = controller.checkAuth(request)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertEmpty(response.errors)
        assertNotNull(response.data)
        assertEquals(name, response.data.name)
        assertEquals(email, response.data.email)
        assertEquals(phone, response.data.phone)
    }
}
