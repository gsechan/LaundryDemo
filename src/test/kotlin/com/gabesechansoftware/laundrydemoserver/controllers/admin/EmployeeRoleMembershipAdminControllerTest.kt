package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.authorization.EmployeeRoleMembershipService
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.Employee
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeRole
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeRoleMembership
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
class EmployeeRoleMembershipAdminControllerTest {

    @MockK private lateinit var membershipService: EmployeeRoleMembershipService
    @MockK private lateinit var adminAuthorizationService: AdminAuthorizationService
    @InjectMockKs private lateinit var controller: EmployeeRoleMembershipAdminController

    private val authedAdmin = Admin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")
    private val orgId = UUID.randomUUID()

    private fun allowManage() =
        every { adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.EDIT_ORG), authedAdmin) } returns true
    private fun denyManage() =
        every { adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.EDIT_ORG), authedAdmin) } returns false

    @Test
    fun `assignRole - without permission returns NOT_AUTHORIZED`() {
        denyManage()
        val response = controller.assignRole(orgId, CreateEmployeeMembershipRequest(UUID.randomUUID(), UUID.randomUUID(), null), authedAdmin)
        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { membershipService.assignRole(any(), any(), any()) }
    }

    @Test
    fun `assignRole - with permission and no locationId assigns org-wide`() {
        val employeeId = UUID.randomUUID()
        val roleId = UUID.randomUUID()
        val employee = Employee(name = "Alice", email = "a@a.com", phone = "2065551111", organizationId = orgId)
        val role = EmployeeRole(name = "Manager", organizationId = orgId)
        val membership = EmployeeRoleMembership(employee = employee, role = role, locationId = null)
        allowManage()
        every { membershipService.assignRole(employeeId, roleId, null) } returns membership

        val response = controller.assignRole(orgId, CreateEmployeeMembershipRequest(employeeId, roleId, null), authedAdmin)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertNull(response.data!!.locationId)
    }

    @Test
    fun `assignRole - with permission and locationId assigns location-scoped`() {
        val employeeId = UUID.randomUUID()
        val roleId = UUID.randomUUID()
        val locationId = UUID.randomUUID()
        val employee = Employee(name = "Alice", email = "a@a.com", phone = "2065551111", organizationId = orgId)
        val role = EmployeeRole(name = "Manager", organizationId = orgId)
        val membership = EmployeeRoleMembership(employee = employee, role = role, locationId = locationId)
        allowManage()
        every { membershipService.assignRole(employeeId, roleId, locationId) } returns membership

        val response = controller.assignRole(orgId, CreateEmployeeMembershipRequest(employeeId, roleId, locationId), authedAdmin)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertEquals(locationId.toString(), response.data!!.locationId)
    }

    @Test
    fun `removeMembership - without permission returns NOT_AUTHORIZED`() {
        denyManage()
        val response = controller.removeMembership(orgId, UUID.randomUUID(), authedAdmin)
        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { membershipService.removeMembership(any()) }
    }

    @Test
    fun `removeMembership - with permission removes and returns success`() {
        val id = UUID.randomUUID()
        allowManage()
        every { membershipService.removeMembership(id) } just Runs

        val response = controller.removeMembership(orgId, id, authedAdmin)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        verify { membershipService.removeMembership(id) }
    }
}
