package com.gabesechansoftware.laundrydemoserver.authorization

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRole
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRolePermission
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRolePermissionRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRoleRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AdminRoleServiceTest {

    @MockK
    private lateinit var adminRoleRepository: AdminRoleRepository

    @MockK
    private lateinit var permissionRepository: AdminRolePermissionRepository

    @InjectMockKs
    private lateinit var service: AdminRoleService

    @Test
    fun `listAll - returns roles with their permissions`() {
        val role = AdminRole(name = "Customer Service")
        every { adminRoleRepository.findAll() } returns listOf(role)
        every { permissionRepository.findByRoleId(role.id) } returns listOf(
            AdminRolePermission(role = role, permission = AdminPermissions.CREATE_ORG),
            AdminRolePermission(role = role, permission = AdminPermissions.EDIT_ORG),
        )

        val result = service.listAll()

        assertSize(1, result)
        assertEquals("Customer Service", result[0].role.name)
        assertEquals(listOf(AdminPermissions.CREATE_ORG, AdminPermissions.EDIT_ORG), result[0].permissions)
    }

    @Test
    fun `createRole - saves the role and a row per permission`() {
        every { adminRoleRepository.save(any()) } returnsArgument 0
        every { permissionRepository.save(any()) } returnsArgument 0

        val result = service.createRole(UploadAdminRole("Customer Service", listOf(AdminPermissions.CREATE_ORG, AdminPermissions.EDIT_ORG)))

        assertEquals("Customer Service", result.role.name)
        assertEquals(listOf(AdminPermissions.CREATE_ORG, AdminPermissions.EDIT_ORG), result.permissions)
        verify { adminRoleRepository.save(result.role) }
        verify(exactly = 2) { permissionRepository.save(any()) }
    }

    @Test
    fun `createRole - with a db-only permission throws and nothing is saved`() {
        assertThrows<APIErrorException> {
            service.createRole(UploadAdminRole("Bad", listOf(AdminPermissions.CREATE_ORG, AdminPermissions.CREATE_ADMIN)))
        }
        verify(exactly = 0) { adminRoleRepository.save(any()) }
        verify(exactly = 0) { permissionRepository.save(any()) }
    }

    @Test
    fun `updateRole - with a db-only permission throws and nothing is changed`() {
        val id = UUID.randomUUID()

        assertThrows<APIErrorException> {
            service.updateRole(id, PatchAdminRole(name = "Role", permissions = listOf(AdminPermissions.ASSIGN_ADMIN_ROLES)))
        }
        verify(exactly = 0) { adminRoleRepository.findById(any()) }
        verify(exactly = 0) { permissionRepository.save(any()) }
    }

    @Test
    fun `updateRole - replaces permissions when provided`() {
        val role = AdminRole(name = "Customer Service")
        every { adminRoleRepository.findById(role.id) } returns Optional.of(role)
        every { adminRoleRepository.save(any()) } returnsArgument 0
        every { permissionRepository.findByRoleId(role.id) } returns listOf(
            AdminRolePermission(role = role, permission = AdminPermissions.CREATE_ORG)
        )
        every { permissionRepository.deleteAll(any()) } just Runs
        every { permissionRepository.save(any()) } returnsArgument 0

        val result = service.updateRole(role.id, PatchAdminRole(name = "CS Renamed", permissions = listOf(AdminPermissions.DELETE_ORG)))

        assertEquals("CS Renamed", result.role.name)
        assertEquals(listOf(AdminPermissions.DELETE_ORG), result.permissions)
        verify { permissionRepository.deleteAll(any()) }
        verify { permissionRepository.save(any()) }
    }

    @Test
    fun `updateRole - null permissions leaves existing permissions untouched`() {
        val role = AdminRole(name = "Customer Service")
        every { adminRoleRepository.findById(role.id) } returns Optional.of(role)
        every { adminRoleRepository.save(any()) } returnsArgument 0
        every { permissionRepository.findByRoleId(role.id) } returns listOf(
            AdminRolePermission(role = role, permission = AdminPermissions.CREATE_ORG)
        )

        val result = service.updateRole(role.id, PatchAdminRole(name = "CS Renamed", permissions = null))

        assertEquals("CS Renamed", result.role.name)
        assertEquals(listOf(AdminPermissions.CREATE_ORG), result.permissions)
        verify(exactly = 0) { permissionRepository.deleteAll(any()) }
    }

    @Test
    fun `updateRole - missing role throws`() {
        val id = UUID.randomUUID()
        every { adminRoleRepository.findById(id) } returns Optional.empty()

        assertThrows<EntityDoesNotExistException> {
            service.updateRole(id, PatchAdminRole(name = "x", permissions = null))
        }
    }

    @Test
    fun `deleteRole - existing role is deleted`() {
        val role = AdminRole(name = "Customer Service")
        every { adminRoleRepository.findById(role.id) } returns Optional.of(role)
        every { adminRoleRepository.delete(role) } just Runs

        service.deleteRole(role.id)

        verify { adminRoleRepository.delete(role) }
    }

    @Test
    fun `deleteRole - missing role throws and nothing is deleted`() {
        val id = UUID.randomUUID()
        every { adminRoleRepository.findById(id) } returns Optional.empty()

        assertThrows<EntityDoesNotExistException> {
            service.deleteRole(id)
        }
        verify(exactly = 0) { adminRoleRepository.delete(any()) }
    }
}
