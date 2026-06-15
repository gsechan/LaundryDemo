package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.authorization.AdminRoleService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminRoleWithPermissions
import com.gabesechansoftware.laundrydemoserver.authorization.PatchAdminRole
import com.gabesechansoftware.laundrydemoserver.authorization.UploadAdminRole
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRole
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
class AdminRoleControllerTest {

    @MockK
    private lateinit var adminRoleService: AdminRoleService

    @MockK
    private lateinit var adminAuthorizationService: AdminAuthorizationService

    @InjectMockKs
    private lateinit var controller: AdminRoleController

    private val authedAdmin = Admin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")

    private fun canAssign(value: Boolean) {
        every {
            adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.ASSIGN_ADMIN_ROLES), authedAdmin)
        } returns value
    }

    @Test
    fun `listRoles - without ASSIGN_ADMIN_ROLES returns NOT_AUTHORIZED`() {
        canAssign(false)

        val response = controller.listRoles(authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        assertNull(response.data)
        verify(exactly = 0) { adminRoleService.listAll() }
    }

    @Test
    fun `listRoles - with permission returns role views`() {
        canAssign(true)
        val role = AdminRole(name = "Customer Service")
        every { adminRoleService.listAll() } returns listOf(
            AdminRoleWithPermissions(role, listOf(AdminPermissions.CREATE_ORG, AdminPermissions.EDIT_ORG))
        )

        val response = controller.listRoles(authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertSize(1, response.data)
        assertEquals("Customer Service", response.data[0].name)
        assertEquals(listOf("CREATE_ORG", "EDIT_ORG"), response.data[0].permissions)
    }

    @Test
    fun `createRole - without ASSIGN_ADMIN_ROLES returns NOT_AUTHORIZED and does not create`() {
        canAssign(false)

        val response = controller.createRole(CreateRoleRequest(UploadAdminRole("CS", listOf(AdminPermissions.CREATE_ORG))), authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { adminRoleService.createRole(any()) }
    }

    @Test
    fun `createRole - with permission creates and returns the view`() {
        canAssign(true)
        val upload = UploadAdminRole("CS", listOf(AdminPermissions.CREATE_ORG))
        val role = AdminRole(name = "CS")
        every { adminRoleService.createRole(upload) } returns AdminRoleWithPermissions(role, listOf(AdminPermissions.CREATE_ORG))

        val response = controller.createRole(CreateRoleRequest(upload), authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertEquals("CS", response.data.name)
        assertEquals(listOf("CREATE_ORG"), response.data.permissions)
    }

    @Test
    fun `updateRole - without ASSIGN_ADMIN_ROLES returns NOT_AUTHORIZED and does not update`() {
        canAssign(false)
        val id = UUID.randomUUID()

        val response = controller.updateRole(id, PatchRoleRequest(PatchAdminRole("CS", null)), authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { adminRoleService.updateRole(any(), any()) }
    }

    @Test
    fun `updateRole - with permission updates and returns the view`() {
        canAssign(true)
        val id = UUID.randomUUID()
        val patch = PatchAdminRole("CS Renamed", listOf(AdminPermissions.DELETE_ORG))
        val role = AdminRole(name = "CS Renamed")
        every { adminRoleService.updateRole(id, patch) } returns AdminRoleWithPermissions(role, listOf(AdminPermissions.DELETE_ORG))

        val response = controller.updateRole(id, PatchRoleRequest(patch), authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertEquals("CS Renamed", response.data.name)
        assertEquals(listOf("DELETE_ORG"), response.data.permissions)
    }

    @Test
    fun `deleteRole - without ASSIGN_ADMIN_ROLES returns NOT_AUTHORIZED and does not delete`() {
        canAssign(false)
        val id = UUID.randomUUID()

        val response = controller.deleteRole(id, authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { adminRoleService.deleteRole(any()) }
    }

    @Test
    fun `deleteRole - with permission deletes and returns success`() {
        canAssign(true)
        val id = UUID.randomUUID()
        every { adminRoleService.deleteRole(id) } just Runs

        val response = controller.deleteRole(id, authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        verify { adminRoleService.deleteRole(id) }
    }
}
