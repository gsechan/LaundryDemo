package com.gabesechansoftware.laundrydemoserver.admins

import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRole
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRoleMembership
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRoleMembershipRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AdminViewMapperTest {

    @MockK
    private lateinit var adminAuthorizationService: AdminAuthorizationService

    @MockK
    private lateinit var membershipRepository: AdminRoleMembershipRepository

    @InjectMockKs
    private lateinit var mapper: AdminViewMapper

    @Test
    fun `toView includes permissions and role memberships`() {
        val admin = Admin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")
        val role = AdminRole(name = "Customer Service")
        val membership = AdminRoleMembership(admin = admin, role = role)
        every { adminAuthorizationService.permissionsFor(admin) } returns
            listOf(AdminPermissions.CREATE_ORG, AdminPermissions.EDIT_ORG)
        every { membershipRepository.findByAdminId(admin.id) } returns listOf(membership)

        val view = mapper.toView(admin)

        assertEquals(admin.id.toString(), view.id)
        assertEquals("Gabe", view.name)
        assertEquals(listOf("CREATE_ORG", "EDIT_ORG"), view.permissions)
        assertEquals(1, view.roleMemberships.size)
        assertEquals(membership.id.toString(), view.roleMemberships[0].membershipId)
        assertEquals(role.id.toString(), view.roleMemberships[0].roleId)
    }
}
