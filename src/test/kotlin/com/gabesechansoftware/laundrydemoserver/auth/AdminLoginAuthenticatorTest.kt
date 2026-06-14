package com.gabesechansoftware.laundrydemoserver.auth

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.DatabaseDataInvalidException
import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminPassword
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminSession
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminPasswordRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminSessionRepository
import com.gabesechansoftware.laundrydemoserver.model.validation.PasswordValidator
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@ExtendWith(MockKExtension::class)
class AdminLoginAuthenticatorTest {

    @MockK
    private lateinit var passwordRepo: AdminPasswordRepository

    @MockK
    private lateinit var sessionRepository: AdminSessionRepository

    @InjectMockKs
    private lateinit var authService: AdminLoginAuthenticator

    private val bcrypt = BCryptPasswordEncoder()
    private val email = "admin@provider.com"
    private val unhashedPassword = "password123"
    private val hashedPassword = bcrypt.encode(unhashedPassword)

    private val admin = Admin(name = "Gabe", email = email, phone = "2067140469")
    private val password = AdminPassword(hash = hashedPassword, admin = admin)

    @Test
    fun `authenticatePassword matching password returns Admin`() {
        every { passwordRepo.findByEmail(email) } returns password

        val result = authService.authenticatePassword(email, unhashedPassword)
        assertEquals(admin, result)
    }

    @Test
    fun `authenticatePassword - non matching password throws BadLoginException`() {
        every { passwordRepo.findByEmail(email) } returns password

        assertThrows<BadLoginException> {
            authService.authenticatePassword(email, "wrong")
        }

        verify(exactly = 0) { sessionRepository.save(any()) }
    }

    @Test
    fun `authenticatePassword - no password found throws BadLoginException`() {
        every { passwordRepo.findByEmail(email) } returns null

        assertThrows<BadLoginException> {
            authService.authenticatePassword(email, "wrong")
        }

        verify(exactly = 0) { sessionRepository.save(any()) }
    }

    @Test
    fun `createSession - saves and returns an admin session`() {
        every { sessionRepository.save(any()) } returnsArgument 0
        val timeSource = mockk<TimeSource>()
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        every { timeSource.now() } returns now
        val service = AdminLoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, timeSource = timeSource)

        val session = service.createSession(admin)
        assertNotNull(session.token)
        assertEquals(admin, session.admin)
        assertNotNull(session.expiration)
        assertEquals(now.plusHours(1), session.expiration)

        verify(exactly = 1) { sessionRepository.save(session) }
    }

    @Test
    fun `createSession - creates unique tokens`() {
        every { sessionRepository.save(any()) } returnsArgument 0

        val session1 = authService.createSession(admin)
        val session2 = authService.createSession(admin)
        assertNotEquals(session1.token, session2.token)
    }

    @Test
    fun `authenticateToken - if session exists for token, return admin and update expiration`() {
        val token = "21b669ee-867f-4748-b859-5058e928bf5b"
        val expire = OffsetDateTime.now(ZoneOffset.UTC)
        val session = AdminSession(admin = admin, token = token, expiration = expire.plusDays(1))
        every { sessionRepository.findByToken(token) } returns listOf(session)
        every { sessionRepository.save(any()) } returnsArgument 0

        val timeSource = mockk<TimeSource>()
        every { timeSource.now() } returns expire
        val service = AdminLoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, timeSource = timeSource)

        val result = service.authenticateToken(token)
        assertEquals(admin, result)
        verify { sessionRepository.save(session) }
        assertEquals(expire.plusHours(1), session.expiration)
    }

    @Test
    fun `authenticateToken - if token has no session, throw BadAuthTokenException`() {
        val token = "21b669ee-867f-4748-b859-5058e928bf5b"
        every { sessionRepository.findByToken(token) } returns emptyList()
        every { sessionRepository.save(any()) } returnsArgument 0

        assertThrows<BadAuthTokenException> {
            authService.authenticateToken(token)
        }
    }

    @Test
    fun `authenticateToken - if token has 2 sessions, throw and delete both`() {
        val token = "21b669ee-867f-4748-b859-5058e928bf5b"
        val expire = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1)
        val session = AdminSession(admin = admin, token = token, expiration = expire)
        val session2 = AdminSession(admin = admin, token = token, expiration = expire)
        every { sessionRepository.findByToken(token) } returns listOf(session, session2)
        every { sessionRepository.save(any()) } returnsArgument 0
        every { sessionRepository.deleteByToken(any()) } returns Unit

        assertThrows<DatabaseDataInvalidException> {
            authService.authenticateToken(token)
        }
        verify(exactly = 1) { sessionRepository.deleteByToken(token) }
    }

    @Test
    fun `authenticateToken - expired token throws BadLoginException and deletes session`() {
        val token = "21b669ee-867f-4748-b859-5058e928bf5b"
        val expire = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1)
        val session = AdminSession(admin = admin, token = token, expiration = expire)
        every { sessionRepository.findByToken(token) } returns listOf(session)
        every { sessionRepository.save(any()) } returnsArgument 0
        every { sessionRepository.deleteByToken(any()) } returns Unit

        assertThrows<BadLoginException> {
            authService.authenticateToken(token)
        }
        verify(exactly = 1) { sessionRepository.deleteByToken(token) }
    }

    @Test
    fun `logout-  all rows deleted`() {
        val token = "21b669ee-867f-4748-b859-5058e928bf5b"
        every { sessionRepository.deleteByToken(any()) } returns Unit

        authService.logout(token)
        verify { sessionRepository.deleteByToken(token) }
    }

    @Test
    fun `createPasswordForAdmin-  password is saved, in an encoded form`() {
        val encoder = mockk<PasswordEncoder>()
        every { passwordRepo.save(any()) } returnsArgument 0
        every { encoder.encode(any()) } returns hashedPassword
        val service = AdminLoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, encoder = encoder)
        service.createPasswordForAdmin(admin, unhashedPassword)

        verify { passwordRepo.save(
            match { it.hash == hashedPassword }
        ) }
        verify { encoder.encode(unhashedPassword) }
    }

    @Test
    fun `createPasswordForAdmin-  invalid password throws APIException`() {
        val validator = mockk<PasswordValidator>()
        every { passwordRepo.save(any()) } returnsArgument 0
        every { validator.validatePassword(any(), any()) } answers { (args[1] as MutableList<String>).add("Error") }

        val service = AdminLoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, passwordValidator = validator)
        assertThrows<APIErrorException> {
            service.createPasswordForAdmin(admin, unhashedPassword)
        }
    }

    @Test
    fun `updatePasswordForAdmin-  if previous password does not exist, throw`() {
        every { passwordRepo.findByEmail(any()) } returns null

        assertThrows<DatabaseDataInvalidException> {
            authService.updatePasswordForAdmin(admin, unhashedPassword)
        }
    }

    @Test
    fun `updatePasswordForAdmin-  if validator fails, throw`() {
        val validator = mockk<PasswordValidator>()
        every { validator.validatePassword(any(), any()) } answers { (args[1] as MutableList<String>).add("Error") }
        every { passwordRepo.findByEmail(any()) } returns AdminPassword()

        val service = AdminLoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, passwordValidator = validator)
        assertThrows<APIErrorException> {
            service.updatePasswordForAdmin(admin, unhashedPassword)
        }
    }

    @Test
    fun `updatePasswordForAdmin-  if everything valid, write to db`() {
        val encoder = mockk<PasswordEncoder>()
        every { encoder.encode(any()) } returns hashedPassword
        every { passwordRepo.findByEmail(any()) } returns AdminPassword()
        every { passwordRepo.save(any()) } returnsArgument 0

        val service = AdminLoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, encoder = encoder)
        service.updatePasswordForAdmin(admin, unhashedPassword)

        verify { passwordRepo.save(match { it.hash == hashedPassword }) }
    }
}
