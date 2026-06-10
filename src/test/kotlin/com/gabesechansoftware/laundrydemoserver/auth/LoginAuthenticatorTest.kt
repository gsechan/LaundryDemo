package com.gabesechansoftware.laundrydemoserver.auth

import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Password
import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Session
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.PasswordRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.SessionRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.UserRepository
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
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class LoginAuthenticatorTest {

    @MockK
    private lateinit var passwordRepo: PasswordRepository

    @MockK
    private lateinit var sessionRepository: SessionRepository

    @MockK
    private lateinit var userRepository: UserRepository

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
    fun `authenticateLoginAndCreateSession - matching password returns UserSession`() {
        every { passwordRepo.findByOrganizationIdAndPhone(orgId, phone) } returns password
        every { sessionRepository.save(any()) } returnsArgument 0
        every { userRepository.getReferenceById(userId) } returns user


        val result = authService.authenticatePassword(orgId, phone, unhashedPassword)

        assertNotNull(result.token)
        assertEquals(user, result.user)
        verify { sessionRepository.save(any()) }
    }

    @Test
    fun `authenticateLoginAndCreateSession - non matching password throws BadLoginException`() {
        every { passwordRepo.findByOrganizationIdAndPhone(orgId, phone) } returns password

        assertThrows<BadLoginException> {
            authService.authenticatePassword(orgId, phone, "wrong")
        }

        verify(exactly = 0) { sessionRepository.save(any()) }
    }

    @Test
    fun `authenticateLoginAndCreateSession - user not found throws BadLoginException`() {
        every { passwordRepo.findByOrganizationIdAndPhone(orgId, phone) } returns null

        assertThrows<BadLoginException> {
            authService.authenticatePassword(orgId, phone, unhashedPassword)
        }

        verify(exactly = 0) { sessionRepository.save(any()) }
    }

    @Test
    fun `authenticateLoginAndCreateSession - generates unique token each call`() {
        every { passwordRepo.findByOrganizationIdAndPhone(orgId, phone) } returns password
        every { sessionRepository.save(any()) } returnsArgument 0
        every { userRepository.getReferenceById(userId) } returns user

        val result1 = authService.authenticatePassword(orgId, phone, unhashedPassword)
        val result2 = authService.authenticatePassword(orgId, phone, unhashedPassword)

        assertNotEquals(result1.token, result2.token)
    }

    @Test
    fun `authenticateLoginAndCreateSession - session expiry is in the future`() {
        every { passwordRepo.findByOrganizationIdAndPhone(orgId, phone) } returns password
        every { sessionRepository.save(any()) } answers {
            val session: Session = firstArg()
            assertTrue(session.expiration!!.isAfter(OffsetDateTime.now()))
            session
        }
        every { userRepository.getReferenceById(userId) } returns user

        authService.authenticatePassword(orgId, phone, unhashedPassword)
    }
}