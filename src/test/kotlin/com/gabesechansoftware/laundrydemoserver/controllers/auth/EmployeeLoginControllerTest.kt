package com.gabesechansoftware.laundrydemoserver.controllers.auth

import com.gabesechansoftware.laundrydemoserver.authentication.BadAuthTokenException
import com.gabesechansoftware.laundrydemoserver.authentication.BadLoginException
import com.gabesechansoftware.laundrydemoserver.authentication.EmployeeLoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.employees.EmployeeView
import com.gabesechansoftware.laundrydemoserver.employees.EmployeeViewMapper
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.Employee
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeSession
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class EmployeeLoginControllerTest {

    @MockK
    private lateinit var employeeLoginAuthenticator: EmployeeLoginAuthenticator

    @MockK
    private lateinit var employeeViewMapper: EmployeeViewMapper

    @InjectMockKs
    private lateinit var controller: EmployeeLoginController

    private val orgId = UUID.randomUUID()
    private val employee = Employee(name = "Alice", email = "alice@example.com", phone = "2067140469", organizationId = orgId)
    private val employeeView = EmployeeView(
        id = employee.id.toString(),
        name = employee.name,
        email = employee.email,
        phone = employee.phone,
        organizationId = orgId.toString(),
    )
    private val session = EmployeeSession(employee = employee, token = "test-token")

    @Test
    fun `login - valid credentials return session and employee view`() {
        every { employeeLoginAuthenticator.authenticatePassword("2067140469", "password123") } returns employee
        every { employeeLoginAuthenticator.createSession(employee) } returns session
        every { employeeViewMapper.toView(employee) } returns employeeView

        val response = controller.login(EmployeeLoginRequest("2067140469", "password123"))

        assertNotNull(response.data)
        assertEquals("test-token", response.data!!.session)
        assertEquals(employeeView, response.data!!.employee)
    }

    @Test
    fun `login - bad credentials return BAD_AUTH error`() {
        every { employeeLoginAuthenticator.authenticatePassword(any(), any()) } throws BadLoginException()

        val response = controller.login(EmployeeLoginRequest("2067140469", "wrong"))

        assertNotNull(response.errorType)
        assertEquals(null, response.data)
    }

    @Test
    fun `logout - valid token logs out successfully`() {
        every { employeeLoginAuthenticator.logout("test-token") } returns Unit

        val response = controller.logout("Bearer test-token")

        assertNotNull(response.data)
        verify { employeeLoginAuthenticator.logout("test-token") }
    }

    @Test
    fun `logout - invalid token returns BAD_AUTH error`() {
        every { employeeLoginAuthenticator.logout(any()) } throws BadAuthTokenException("bad")

        val response = controller.logout("Bearer bad")

        assertNotNull(response.errorType)
    }

    @Test
    fun `checkAuth - valid token returns employee view`() {
        every { employeeLoginAuthenticator.authenticateToken("test-token") } returns employee
        every { employeeViewMapper.toView(employee) } returns employeeView

        val response = controller.checkAuth(EmployeeCheckAuthRequest("test-token"))

        assertNotNull(response.data)
        assertEquals(employeeView, response.data)
    }

    @Test
    fun `checkAuth - invalid token returns BAD_AUTH error`() {
        every { employeeLoginAuthenticator.authenticateToken(any()) } throws BadAuthTokenException("bad")

        val response = controller.checkAuth(EmployeeCheckAuthRequest("bad"))

        assertNotNull(response.errorType)
        assertEquals(null, response.data)
    }
}
