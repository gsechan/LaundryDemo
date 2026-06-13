package com.gabesechansoftware.laundrydemoserver.auth

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.DatabaseDataInvalidException
import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Password
import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Session
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.PasswordRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.SessionRepository
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
    private val phone = "2067140469"
    private val unhashedPassword = "password123"
    private val hashedPassword = bcrypt.encode(unhashedPassword)

    private val org = Organization()
    private val user = User(phone = phone, organization = org)
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
    fun `authenticatePassword - no password found throws BadLoginException`() {
        every { passwordRepo.findByOrganizationIdAndPhone(orgId, phone) } returns null

        assertThrows<BadLoginException> {
            authService.authenticatePassword(orgId, phone, "wrong")
        }

        verify(exactly = 0) { sessionRepository.save(any()) }
    }

    @Test
    fun `createSession - saves and returns a user session`() {
        every { sessionRepository.save(any()) } returnsArgument 0
        val timeSource = mockk<TimeSource>()
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        every { timeSource.now() } returns now
        val service = LoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, timeSource = timeSource)

        val session = service.createSession(user)
        assertNotNull(session.token)
        assertEquals(user, session.user)
        assertNotNull(session.expiration)
        assertEquals(now.plusYears(1), session.expiration)

        verify(exactly = 1) { sessionRepository.save(session) }
    }

    @Test
    fun `createSession - creates unique tokens`() {
        every { sessionRepository.save(any()) } returnsArgument 0

        val session1 = authService.createSession(user)
        val session2 = authService.createSession(user)
        assertNotEquals(session1.token, session2.token)
    }

    @Test
    fun `authenticateToken - if session exists for token, return user and update expiration`() {
        val token = "21b669ee-867f-4748-b859-5058e928bf5b"
        val expire = OffsetDateTime.now(ZoneOffset.UTC)
        val session = Session(user = user, token = token, expiration = expire.plusDays(1))
        every { sessionRepository.findByToken(token) } returns listOf(session)
        every { sessionRepository.save(any()) } returnsArgument 0

        //We mock the time source so we can measure the exact expiration date its set to.  Otherwise, this test
        //would only be able to test if the expiration date moved forward at all, or be flaky based on runtime
        val timeSource = mockk<TimeSource>()
        every { timeSource.now() } returns expire
        val service = LoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, timeSource = timeSource)

        val result = service.authenticateToken(token)
        assertEquals(user, result)
        verify { sessionRepository.save(session) }
        assertEquals(  expire.plusYears(1), session.expiration )
    }

    @Test
    fun `authenticateToken - if token has no session, throw BadLoginException`() {
        val token = "21b669ee-867f-4748-b859-5058e928bf5b"
        every { sessionRepository.findByToken(token) } returns emptyList()
        every { sessionRepository.save(any()) } returnsArgument 0

        assertThrows<BadAuthTokenException> {
            authService.authenticateToken(token)
        }
    }

    @Test
    fun `authenticateToken - if token has 2 sessions, throw BadLoginException and delete both`() {
        val token = "21b669ee-867f-4748-b859-5058e928bf5b"
        val expire = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1)
        val session = Session(user = user, token = token, expiration = expire)
        val session2 = Session(user = user, token = token, expiration = expire)
        every { sessionRepository.findByToken(token) } returns listOf(session, session2)
        every { sessionRepository.save(any()) } returnsArgument 0
        every { sessionRepository.deleteByToken(any()) } returns  Unit

        assertThrows<DatabaseDataInvalidException> {
            authService.authenticateToken(token)
        }
        verify(exactly = 1) { sessionRepository.deleteByToken(token) }
    }

    @Test
    fun `authenticateToken - expired token throws BadLoginException and deletes session`() {
        val token = "21b669ee-867f-4748-b859-5058e928bf5b"
        val expire = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1)
        val session = Session(user = user, token = token, expiration = expire)
        every { sessionRepository.findByToken(token) } returns listOf(session)
        every { sessionRepository.save(any()) } returnsArgument 0
        every { sessionRepository.deleteByToken(any()) } returns  Unit

        assertThrows<BadLoginException> {
            authService.authenticateToken(token)
        }
        verify(exactly = 1) { sessionRepository.deleteByToken(token) }
    }

    @Test
    fun `logout-  all rows deleted`() {
        val token = "21b669ee-867f-4748-b859-5058e928bf5b"
        every { sessionRepository.deleteByToken(any()) } returns  Unit

        authService.logout(token)
        verify { sessionRepository.deleteByToken(token) }
    }

    @Test
    fun `createPasswordForUser-  password is saved, in an encoded form`() {
        val encoder = mockk<PasswordEncoder>()
        every { passwordRepo.save(any()) } returnsArgument 0
        every { encoder.encode(any()) } returns hashedPassword
        val service = LoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, encoder = encoder)
        service.createPasswordForUser(user, unhashedPassword)

        verify { passwordRepo.save(
            match { it.hash == hashedPassword }
        ) }
        verify { encoder.encode(unhashedPassword) }
    }

    @Test
    fun `createPasswordForUser-  invalid password throws APIException`() {
        val validator = mockk<PasswordValidator>()
        every { passwordRepo.save(any()) } returnsArgument 0
        every { validator.validatePassword(any(), any()) } answers {(args[1] as MutableList<String>).add("Error")}

        val service = LoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, passwordValidator = validator)
        assertThrows<APIErrorException> {
            service.createPasswordForUser(user, unhashedPassword)
        }
    }

    @Test
    fun `updatePasswordForUser-  if previous password does not exist, throw`() {
        every { passwordRepo.findByOrganizationIdAndPhone(any(), any()) } returns null

        assertThrows<DatabaseDataInvalidException> {
            authService.updatePasswordForUser(user, unhashedPassword)
        }
    }

    @Test
    fun `updatePasswordForUser-  if validator fails, throw`() {
        val validator = mockk<PasswordValidator>()
        every { validator.validatePassword(any(), any()) } answers {(args[1] as MutableList<String>).add("Error")}
        every { passwordRepo.findByOrganizationIdAndPhone(any(), any()) } returns Password()

        val service = LoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, passwordValidator = validator)
        assertThrows<APIErrorException> {
            service.updatePasswordForUser(user, unhashedPassword)
        }
    }

    @Test
    fun `updatePasswordForUser-  if everything valid, write to db`() {
        val encoder = mockk<PasswordEncoder>()
        every { encoder.encode(any()) } returns hashedPassword
        every { passwordRepo.findByOrganizationIdAndPhone(any(), any()) } returns Password()
        every { passwordRepo.save(any()) } returnsArgument 0

        val service = LoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, encoder = encoder)
        service.updatePasswordForUser(user, unhashedPassword)

        verify { passwordRepo.save(match { it.hash == hashedPassword }) }
    }

}