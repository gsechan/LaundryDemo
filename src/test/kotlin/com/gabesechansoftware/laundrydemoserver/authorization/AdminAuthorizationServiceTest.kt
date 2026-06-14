package com.gabesechansoftware.laundrydemoserver.authorization

import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRolePermissionRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class AdminAuthorizationServiceTest {

    @MockK
    private lateinit var permissionRepository: AdminRolePermissionRepository

    @InjectMockKs
    private lateinit var service: AdminAuthorizationService

    private val admin = Admin(name = "Gabe", email = "admin@provider.com", phone = "2067140469")

    private fun adminHas(vararg permissions: AdminPermissions) {
        every { permissionRepository.findPermissionsByAdminId(admin.id) } returns permissions.toList()
    }

    @Test
    fun `permissionsCheckAll - admin has all required, returns true`() {
        adminHas(AdminPermissions.CREATE_ORG, AdminPermissions.EDIT_ORG, AdminPermissions.DELETE_ORG)

        val result = service.permissionsCheckAll(
            listOf(AdminPermissions.CREATE_ORG, AdminPermissions.EDIT_ORG),
            admin
        )
        assertTrue(result)
    }

    @Test
    fun `permissionsCheckAll - admin missing one required, returns false`() {
        adminHas(AdminPermissions.CREATE_ORG)

        val result = service.permissionsCheckAll(
            listOf(AdminPermissions.CREATE_ORG, AdminPermissions.EDIT_ORG),
            admin
        )
        assertFalse(result)
    }

    @Test
    fun `permissionsCheckAll - admin has no permissions, returns false`() {
        adminHas()

        val result = service.permissionsCheckAll(listOf(AdminPermissions.CREATE_ORG), admin)
        assertFalse(result)
    }

    @Test
    fun `permissionsCheckAll - empty list returns true without querying`() {
        val result = service.permissionsCheckAll(emptyList(), admin)

        assertTrue(result)
        verify(exactly = 0) { permissionRepository.findPermissionsByAdminId(any()) }
    }

    @Test
    fun `permissionsCheckAny - admin has one of the required, returns true`() {
        adminHas(AdminPermissions.EDIT_ORG)

        val result = service.permissionsCheckAny(
            listOf(AdminPermissions.CREATE_ORG, AdminPermissions.EDIT_ORG),
            admin
        )
        assertTrue(result)
    }

    @Test
    fun `permissionsCheckAny - admin has none of the required, returns false`() {
        adminHas(AdminPermissions.DELETE_ADMIN)

        val result = service.permissionsCheckAny(
            listOf(AdminPermissions.CREATE_ORG, AdminPermissions.EDIT_ORG),
            admin
        )
        assertFalse(result)
    }

    @Test
    fun `permissionsCheckAny - admin has no permissions, returns false`() {
        adminHas()

        val result = service.permissionsCheckAny(listOf(AdminPermissions.CREATE_ORG), admin)
        assertFalse(result)
    }

    @Test
    fun `permissionsCheckAny - empty list returns true without querying`() {
        val result = service.permissionsCheckAny(emptyList(), admin)

        assertTrue(result)
        verify(exactly = 0) { permissionRepository.findPermissionsByAdminId(any()) }
    }
}
