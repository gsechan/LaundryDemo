package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.authorization.AdminRoleMembershipService
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRole
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRoleMembership
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
class AdminRoleMembershipControllerTest {

    @MockK
    private lateinit var membershipService: AdminRoleMembershipService

    @MockK
    private lateinit var adminAuthorizationService: AdminAuthorizationService

    @InjectMockKs
    private lateinit var controller: AdminRoleMembershipController

    private val authedAdmin = Admin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")

    private fun canAssign(value: Boolean) {
        every {
            adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.ASSIGN_ADMIN_ROLES), authedAdmin)
        } returns value
    }

    @Test
    fun `assignRole - without ASSIGN_ADMIN_ROLES returns NOT_AUTHORIZED and does not assign`() {
        canAssign(false)
        val request = CreateMembershipRequest(UUID.randomUUID(), UUID.randomUUID())

        val response = controller.assignRole(request, authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        assertNull(response.data)
        verify(exactly = 0) { membershipService.assignRole(any(), any()) }
    }

    @Test
    fun `assignRole - with permission assigns and returns the view`() {
        canAssign(true)
        val adminId = UUID.randomUUID()
        val roleId = UUID.randomUUID()
        val target = Admin(name = "Sue", email = "sue@provider.com", phone = "2065551212")
        val role = AdminRole(name = "Customer Service")
        every { membershipService.assignRole(adminId, roleId) } returns AdminRoleMembership(admin = target, role = role)

        val response = controller.assignRole(CreateMembershipRequest(adminId, roleId), authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertEquals(target.id.toString(), response.data.adminId)
        assertEquals(role.id.toString(), response.data.roleId)
        verify { membershipService.assignRole(adminId, roleId) }
    }

    @Test
    fun `removeMembership - without ASSIGN_ADMIN_ROLES returns NOT_AUTHORIZED and does not delete`() {
        canAssign(false)
        val id = UUID.randomUUID()

        val response = controller.removeMembership(id, authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { membershipService.removeMembership(any()) }
    }

    @Test
    fun `removeMembership - with permission deletes and returns success`() {
        canAssign(true)
        val id = UUID.randomUUID()
        every { membershipService.removeMembership(id) } just Runs

        val response = controller.removeMembership(id, authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        verify { membershipService.removeMembership(id) }
    }
}
