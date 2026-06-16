package com.gabesechansoftware.laundrydemoserver.authentication

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.DatabaseDataInvalidException
import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.Employee
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeePassword
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeSession
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee.EmployeePasswordRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee.EmployeeSessionRepository
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
class EmployeeLoginAuthenticatorTest {

    @MockK
    private lateinit var passwordRepo: EmployeePasswordRepository

    @MockK
    private lateinit var sessionRepository: EmployeeSessionRepository

    @InjectMockKs
    private lateinit var authService: EmployeeLoginAuthenticator

    private val bcrypt = BCryptPasswordEncoder()
    private val phone = "2067140469"
    private val unhashedPassword = "password123"
    private val hashedPassword = bcrypt.encode(unhashedPassword)

    private val orgId = UUID.randomUUID()
    private val employee = Employee(name = "Alice", email = "alice@example.com", phone = phone, organizationId = orgId)
    private val password = EmployeePassword(hash = hashedPassword, employee = employee)

    @Test
    fun `authenticatePassword - matching password returns Employee`() {
        every { passwordRepo.findByEmployeePhone(phone) } returns listOf(password)

        val result = authService.authenticatePassword(phone, unhashedPassword)
        assertEquals(employee, result)
    }

    @Test
    fun `authenticatePassword - wrong password throws BadLoginException`() {
        every { passwordRepo.findByEmployeePhone(phone) } returns listOf(password)

        assertThrows<BadLoginException> {
            authService.authenticatePassword(phone, "wrong")
        }
    }

    @Test
    fun `authenticatePassword - no passwords found throws BadLoginException`() {
        every { passwordRepo.findByEmployeePhone(phone) } returns emptyList()

        assertThrows<BadLoginException> {
            authService.authenticatePassword(phone, unhashedPassword)
        }
    }

    @Test
    fun `authenticatePassword - round-robin finds second matching employee`() {
        val employee2 = Employee(name = "Bob", email = "bob@example.com", phone = phone, organizationId = UUID.randomUUID())
        val wrongPassword = EmployeePassword(hash = bcrypt.encode("different"), employee = employee)
        val correctPassword = EmployeePassword(hash = hashedPassword, employee = employee2)

        every { passwordRepo.findByEmployeePhone(phone) } returns listOf(wrongPassword, correctPassword)

        val result = authService.authenticatePassword(phone, unhashedPassword)
        assertEquals(employee2, result)
    }

    @Test
    fun `createSession - saves and returns an employee session`() {
        every { sessionRepository.save(any()) } returnsArgument 0
        val timeSource = mockk<TimeSource>()
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        every { timeSource.now() } returns now
        val service = EmployeeLoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, timeSource = timeSource)

        val session = service.createSession(employee)
        assertNotNull(session.token)
        assertEquals(employee, session.employee)
        assertEquals(now.plusHours(1), session.expiration)

        verify(exactly = 1) { sessionRepository.save(session) }
    }

    @Test
    fun `createSession - creates unique tokens`() {
        every { sessionRepository.save(any()) } returnsArgument 0

        val session1 = authService.createSession(employee)
        val session2 = authService.createSession(employee)
        assertNotEquals(session1.token, session2.token)
    }

    @Test
    fun `authenticateToken - valid token returns employee and updates expiration`() {
        val token = "test-token-uuid"
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val session = EmployeeSession(employee = employee, token = token, expiration = now.plusDays(1))
        every { sessionRepository.findByToken(token) } returns listOf(session)
        every { sessionRepository.save(any()) } returnsArgument 0

        val timeSource = mockk<TimeSource>()
        every { timeSource.now() } returns now
        val service = EmployeeLoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, timeSource = timeSource)

        val result = service.authenticateToken(token)
        assertEquals(employee, result)
        verify { sessionRepository.save(session) }
        assertEquals(now.plusHours(1), session.expiration)
    }

    @Test
    fun `authenticateToken - no session throws BadAuthTokenException`() {
        val token = "nonexistent-token"
        every { sessionRepository.findByToken(token) } returns emptyList()

        assertThrows<BadAuthTokenException> {
            authService.authenticateToken(token)
        }
    }

    @Test
    fun `authenticateToken - duplicate sessions throws and deletes`() {
        val token = "dup-token"
        val expire = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1)
        val session1 = EmployeeSession(employee = employee, token = token, expiration = expire)
        val session2 = EmployeeSession(employee = employee, token = token, expiration = expire)
        every { sessionRepository.findByToken(token) } returns listOf(session1, session2)
        every { sessionRepository.deleteByToken(token) } returns Unit

        assertThrows<DatabaseDataInvalidException> {
            authService.authenticateToken(token)
        }
        verify(exactly = 1) { sessionRepository.deleteByToken(token) }
    }

    @Test
    fun `authenticateToken - expired token throws BadLoginException and deletes session`() {
        val token = "expired-token"
        val expire = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1)
        val session = EmployeeSession(employee = employee, token = token, expiration = expire)
        every { sessionRepository.findByToken(token) } returns listOf(session)
        every { sessionRepository.deleteByToken(token) } returns Unit

        assertThrows<BadLoginException> {
            authService.authenticateToken(token)
        }
        verify(exactly = 1) { sessionRepository.deleteByToken(token) }
    }

    @Test
    fun `logout - deletes session by token`() {
        val token = "some-token"
        every { sessionRepository.deleteByToken(token) } returns Unit

        authService.logout(token)
        verify { sessionRepository.deleteByToken(token) }
    }

    @Test
    fun `createPasswordForEmployee - password is saved encoded`() {
        val encoder = mockk<PasswordEncoder>()
        every { passwordRepo.save(any()) } returnsArgument 0
        every { encoder.encode(any()) } returns hashedPassword

        val service = EmployeeLoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, encoder = encoder)
        service.createPasswordForEmployee(employee, unhashedPassword)

        verify { passwordRepo.save(match { it.hash == hashedPassword }) }
        verify { encoder.encode(unhashedPassword) }
    }

    @Test
    fun `createPasswordForEmployee - invalid password throws APIErrorException`() {
        val validator = mockk<PasswordValidator>()
        every { validator.validatePassword(any(), any()) } answers { (args[1] as MutableList<String>).add("Error") }

        val service = EmployeeLoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, passwordValidator = validator)
        assertThrows<APIErrorException> {
            service.createPasswordForEmployee(employee, unhashedPassword)
        }
    }

    @Test
    fun `updatePasswordForEmployee - no existing password throws DatabaseDataInvalidException`() {
        every { passwordRepo.findByEmployeeId(employee.id) } returns null

        assertThrows<DatabaseDataInvalidException> {
            authService.updatePasswordForEmployee(employee, unhashedPassword)
        }
    }

    @Test
    fun `updatePasswordForEmployee - invalid password throws APIErrorException`() {
        val validator = mockk<PasswordValidator>()
        every { validator.validatePassword(any(), any()) } answers { (args[1] as MutableList<String>).add("Error") }
        every { passwordRepo.findByEmployeeId(employee.id) } returns EmployeePassword()

        val service = EmployeeLoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, passwordValidator = validator)
        assertThrows<APIErrorException> {
            service.updatePasswordForEmployee(employee, unhashedPassword)
        }
    }

    @Test
    fun `updatePasswordForEmployee - valid password updates hash in db`() {
        val encoder = mockk<PasswordEncoder>()
        every { encoder.encode(any()) } returns hashedPassword
        every { passwordRepo.findByEmployeeId(employee.id) } returns EmployeePassword()
        every { passwordRepo.save(any()) } returnsArgument 0

        val service = EmployeeLoginAuthenticator(passwordRepo = passwordRepo, sessionRepository = sessionRepository, encoder = encoder)
        service.updatePasswordForEmployee(employee, unhashedPassword)

        verify { passwordRepo.save(match { it.hash == hashedPassword }) }
    }
}
