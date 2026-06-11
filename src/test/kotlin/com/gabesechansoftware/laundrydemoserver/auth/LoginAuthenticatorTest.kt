package com.gabesechansoftware.laundrydemoserver.auth

import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Password
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.PasswordRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.SessionRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@ExtendWith(MockKExtension::class)
class LoginAuthenticatorTest {

    @MockK
    private lateinit var passwordRepo: PasswordRepository

    @MockK
    private lateinit var sessionRepository: SessionRepository

    @InjectMockKs
    private lateinit var authService: LoginAuthenticator

    private val bcrypt = BCryptPasswordEncoder()
    private val orgId = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private val phone = "2067140469"
    private val unhashedPassword = "password123"
    private val hashedPassword = bcrypt.encode(unhashedPassword)

    private val user = User(phone = phone).apply { id = userId }
    private val password = Password(hash = hashedPassword, user = user)

    @Test
    fun `authenticatePassword matching password returns User`() {
        every { passwordRepo.findByOrganizationIdAndPhone(orgId, phone) } returns password

        val result = authService.authenticatePassword(orgId, phone, unhashedPassword)
        assertEquals(user, result)

    }

    @Test
    fun `authenticatePassword - non matching password throws BadLoginException`() {
        every { passwordRepo.findByOrganizationIdAndPhone(orgId, phone) } returns password

        assertThrows<BadLoginException> {
            authService.authenticatePassword(orgId, phone, "wrong")
        }

        verify(exactly = 0) { sessionRepository.save(any()) }
    }

    @Test
    fun `createSession - saves and returns a user session`() {
        every { sessionRepository.save(any()) } returnsArgument 0

        val session = authService.createSession(user)
        assertNotNull(session.token)
        assertEquals(user, session.user)
        assertNotNull(session.expiration)
        assert(OffsetDateTime.now(ZoneOffset.UTC).isBefore(session.expiration))

        verify(exactly = 1) { sessionRepository.save(session) }
    }

    @Test
    fun `createSession - creates unique tokens`() {
        every { sessionRepository.save(any()) } returnsArgument 0

        val session1 = authService.createSession(user)
        val session2 = authService.createSession(user)
        assertNotEquals(session1.token, session2.token)
    }

    //AuthenticateToken-  session exists returns user, saves with updated expiration
    //AuthenticateToken-  session does not exist, throws BAD_AUTH
    //AuthenticateToken-  token exists twice, throws DatabaseDataInvalidException
    //AuthenticateToken-  expired token, throws bad auth

    //logout-  row deleted.   FIX NEEDED-  If the token exists twice, delete both.  Do we want to do that for Auth as well?

    //setPassword-  move to

}