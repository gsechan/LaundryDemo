package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.authorization.EmployeePermissions
import com.gabesechansoftware.laundrydemoserver.authorization.EmployeeRoleService
import com.gabesechansoftware.laundrydemoserver.authorization.EmployeeRoleWithPermissions
import com.gabesechansoftware.laundrydemoserver.authorization.PatchEmployeeRole
import com.gabesechansoftware.laundrydemoserver.authorization.UploadEmployeeRole
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeRole
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
class EmployeeRoleAdminControllerTest {

    @MockK private lateinit var employeeRoleService: EmployeeRoleService
    @MockK private lateinit var adminAuthorizationService: AdminAuthorizationService
    @InjectMockKs private lateinit var controller: EmployeeRoleAdminController

    private val authedAdmin = Admin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")
    private val orgId = UUID.randomUUID()

    private fun allowManage() =
        every { adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.EDIT_ORG), authedAdmin) } returns true
    private fun denyManage() =
        every { adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.EDIT_ORG), authedAdmin) } returns false

    private fun roleWithPerms(name: String, vararg perms: EmployeePermissions) =
        EmployeeRoleWithPermissions(EmployeeRole(name = name, organizationId = orgId), perms.toList())

    @Test
    fun `listRoles - without permission returns NOT_AUTHORIZED`() {
        denyManage()
        val response = controller.listRoles(orgId, authedAdmin)
        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        assertNull(response.data)
    }

    @Test
    fun `listRoles - with permission returns role views`() {
        allowManage()
        every { employeeRoleService.listByOrganization(orgId) } returns listOf(
            roleWithPerms("Manager", EmployeePermissions.CREATE_ITEM),
            roleWithPerms("Viewer"),
        )
        val response = controller.listRoles(orgId, authedAdmin)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertSize(2, response.data!!)
    }

    @Test
    fun `createRole - without permission returns NOT_AUTHORIZED`() {
        denyManage()
        val response = controller.createRole(orgId, CreateEmployeeRoleRequest(UploadEmployeeRole("R", emptyList())), authedAdmin)
        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { employeeRoleService.createRole(any(), any()) }
    }

    @Test
    fun `createRole - with permission creates and returns view`() {
        val upload = UploadEmployeeRole("Manager", listOf(EmployeePermissions.CREATE_ITEM))
        val created = roleWithPerms("Manager", EmployeePermissions.CREATE_ITEM)
        allowManage()
        every { employeeRoleService.createRole(orgId, upload) } returns created

        val response = controller.createRole(orgId, CreateEmployeeRoleRequest(upload), authedAdmin)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertEquals("Manager", response.data!!.name)
        assertEquals(listOf("CREATE_ITEM"), response.data!!.permissions)
    }

    @Test
    fun `updateRole - without permission returns NOT_AUTHORIZED`() {
        denyManage()
        val response = controller.updateRole(orgId, UUID.randomUUID(), PatchEmployeeRoleRequest(PatchEmployeeRole(null, null)), authedAdmin)
        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { employeeRoleService.updateRole(any(), any(), any()) }
    }

    @Test
    fun `updateRole - with permission updates and returns view`() {
        val id = UUID.randomUUID()
        val patch = PatchEmployeeRole("Senior Manager", null)
        val updated = roleWithPerms("Senior Manager", EmployeePermissions.CREATE_ITEM)
        allowManage()
        every { employeeRoleService.updateRole(orgId, id, patch) } returns updated

        val response = controller.updateRole(orgId, id, PatchEmployeeRoleRequest(patch), authedAdmin)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertEquals("Senior Manager", response.data!!.name)
    }

    @Test
    fun `deleteRole - without permission returns NOT_AUTHORIZED`() {
        denyManage()
        val response = controller.deleteRole(orgId, UUID.randomUUID(), authedAdmin)
        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { employeeRoleService.deleteRole(any(), any()) }
    }

    @Test
    fun `deleteRole - with permission deletes and returns success`() {
        val id = UUID.randomUUID()
        allowManage()
        every { employeeRoleService.deleteRole(orgId, id) } just Runs

        val response = controller.deleteRole(orgId, id, authedAdmin)
        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        verify { employeeRoleService.deleteRole(orgId, id) }
    }
}
