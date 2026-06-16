package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.employees.EmployeeService
import com.gabesechansoftware.laundrydemoserver.employees.EmployeeView
import com.gabesechansoftware.laundrydemoserver.employees.EmployeeViewMapper
import com.gabesechansoftware.laundrydemoserver.employees.PatchEmployee
import com.gabesechansoftware.laundrydemoserver.employees.UploadEmployee
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.Employee
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.Runs
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
class EmployeeAdminControllerTest {

    @MockK private lateinit var employeeService: EmployeeService
    @MockK private lateinit var employeeViewMapper: EmployeeViewMapper
    @MockK private lateinit var adminAuthorizationService: AdminAuthorizationService
    @InjectMockKs private lateinit var controller: EmployeeAdminController

    private val authedAdmin = Admin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")
    private val orgId = UUID.randomUUID()

    private fun stubView(employee: Employee) =
        EmployeeView(employee.id.toString(), employee.name, employee.email, employee.phone, employee.organizationId?.toString())
            .also { every { employeeViewMapper.toView(employee) } returns it }

    private fun allowEditOrg() =
        every { adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.EDIT_ORG), authedAdmin) } returns true
    private fun denyEditOrg() =
        every { adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.EDIT_ORG), authedAdmin) } returns false
    private fun allowListOrg() =
        every { adminAuthorizationService.permissionsCheckAny(listOf(AdminPermissions.EDIT_ORG, AdminPermissions.CREATE_ORG), authedAdmin) } returns true
    private fun denyListOrg() =
        every { adminAuthorizationService.permissionsCheckAny(listOf(AdminPermissions.EDIT_ORG, AdminPermissions.CREATE_ORG), authedAdmin) } returns false

    @Test
    fun `listEmployees - without permission returns NOT_AUTHORIZED`() {
        denyListOrg()
        val response = controller.listEmployees(orgId, authedAdmin)
        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        assertNull(response.data)
    }

    @Test
    fun `listEmployees - with permission returns employee views`() {
        val e1 = Employee(name = "Alice", email = "a@a.com", phone = "2065551111", organizationId = orgId)
        val e2 = Employee(name = "Bob", email = "b@b.com", phone = "2065552222", organizationId = orgId)
        allowListOrg()
        every { employeeService.listByOrganization(orgId) } returns listOf(e1, e2)
        stubView(e1); stubView(e2)

        val response = controller.listEmployees(orgId, authedAdmin)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertSize(2, response.data!!)
    }

    @Test
    fun `createEmployee - without permission returns NOT_AUTHORIZED`() {
        denyEditOrg()
        val response = controller.createEmployee(orgId, CreateEmployeeRequest(UploadEmployee("A", "a@a.com", "2065551111"), "pass"), authedAdmin)
        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { employeeService.createEmployee(any(), any(), any()) }
    }

    @Test
    fun `createEmployee - with permission creates and returns view`() {
        val upload = UploadEmployee("Alice", "alice@a.com", "2065551111")
        val employee = Employee(name = "Alice", email = "alice@a.com", phone = "2065551111", organizationId = orgId)
        allowEditOrg()
        every { employeeService.createEmployee(orgId, upload, "pass123") } returns employee
        stubView(employee)

        val response = controller.createEmployee(orgId, CreateEmployeeRequest(upload, "pass123"), authedAdmin)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertEquals("Alice", response.data!!.name)
    }

    @Test
    fun `updateEmployee - without permission returns NOT_AUTHORIZED`() {
        denyEditOrg()
        val response = controller.updateEmployee(orgId, UUID.randomUUID(), PatchEmployeeRequest(PatchEmployee(null, null, null)), authedAdmin)
        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { employeeService.updateEmployee(any(), any(), any()) }
    }

    @Test
    fun `updateEmployee - with permission updates and returns view`() {
        val id = UUID.randomUUID()
        val patch = PatchEmployee("Bob", null, null)
        val updated = Employee(name = "Bob", email = "alice@a.com", phone = "2065551111", organizationId = orgId)
        allowEditOrg()
        every { employeeService.updateEmployee(orgId, id, patch) } returns updated
        stubView(updated)

        val response = controller.updateEmployee(orgId, id, PatchEmployeeRequest(patch), authedAdmin)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertEquals("Bob", response.data!!.name)
    }

    @Test
    fun `deleteEmployee - without permission returns NOT_AUTHORIZED`() {
        denyEditOrg()
        val response = controller.deleteEmployee(orgId, UUID.randomUUID(), authedAdmin)
        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { employeeService.deleteEmployee(any(), any()) }
    }

    @Test
    fun `deleteEmployee - with permission deletes and returns success`() {
        val id = UUID.randomUUID()
        allowEditOrg()
        every { employeeService.deleteEmployee(orgId, id) } just Runs

        val response = controller.deleteEmployee(orgId, id, authedAdmin)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        verify { employeeService.deleteEmployee(orgId, id) }
    }
}
