package com.gabesechansoftware.laundrydemoserver.authentication

import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.ModelAndViewContainer
import tools.jackson.databind.ObjectMapper
import java.io.PrintWriter
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AuthenticatedUserResolverTest {

    @MockK
    private lateinit var userLoginAuthenticator: UserLoginAuthenticator

    @MockK
    private lateinit var objectMapper: ObjectMapper

    @InjectMockKs
    private lateinit var resolver: AuthenticatedUserResolver

    @MockK
    private lateinit var parameter: MethodParameter

    @MockK
    private lateinit var mavContainer: ModelAndViewContainer

    @MockK
    private lateinit var webRequest: NativeWebRequest

    @MockK
    private lateinit var httpResponse: HttpServletResponse

    @MockK
    private lateinit var writer: PrintWriter

    private val user = User(name = "Gabe", phone = "2067140469")

    private fun setupFailureResponse() {
        every { webRequest.getNativeResponse(HttpServletResponse::class.java) } returns httpResponse
        every { httpResponse.status = any() } just Runs
        every { httpResponse.contentType = any() } just Runs
        every { httpResponse.writer } returns writer
        every { writer.write(any<String>()) } just Runs
        every { writer.flush() } just Runs
        every { objectMapper.writeValueAsString(any()) } returns "{}"
        every { mavContainer.isRequestHandled = true } just Runs
    }

    @Test
    fun `resolveArgument - no authorization header throws BadAuthTokenException`() {
        every { webRequest.getHeader("Authorization") } returns null
        setupFailureResponse()

        assertThrows<BadAuthTokenException> {
            resolver.resolveArgument(parameter, mavContainer, webRequest, null)
        }

        verify { mavContainer.isRequestHandled = true }
        verify(exactly = 0) { userLoginAuthenticator.authenticateToken(any()) }
    }

    @Test
    fun `resolveArgument - header without Bearer prefix throws BadAuthTokenException`() {
        every { webRequest.getHeader("Authorization") } returns "sometoken123"
        every { userLoginAuthenticator.authenticateToken("sometoken123") } throws BadAuthTokenException("sometoken123")
        setupFailureResponse()

        assertThrows<BadAuthTokenException> {
            resolver.resolveArgument(parameter, mavContainer, webRequest, null)
        }
    }

    @Test
    fun `resolveArgument - valid Bearer token returns user from authenticateToken`() {
        every { webRequest.getHeader("Authorization") } returns "Bearer validtoken123"
        every { userLoginAuthenticator.authenticateToken("validtoken123") } returns user

        val result = resolver.resolveArgument(parameter, mavContainer, webRequest, null)

        assertEquals(user, result)

        verify { userLoginAuthenticator.authenticateToken("validtoken123") }
    }

    @Test
    fun `resolveArgument - no header sets response status to 200`() {
        every { webRequest.getHeader("Authorization") } returns null
        setupFailureResponse()

        assertThrows<BadAuthTokenException> {
            resolver.resolveArgument(parameter, mavContainer, webRequest, null)
        }

        verify { httpResponse.status = 200 }
        verify { httpResponse.contentType = "application/json" }
    }
}