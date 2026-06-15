package com.gabesechansoftware.laundrydemoserver.authorization

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRole
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRolePermission
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRolePermissionRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRoleRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

data class UploadAdminRole(
    val name: String,
    val permissions: List<AdminPermissions>,
)

data class PatchAdminRole(
    val name: String?,
    val permissions: List<AdminPermissions>?,
)

data class AdminRoleWithPermissions(
    val role: AdminRole,
    val permissions: List<AdminPermissions>,
)

// These permissions are powerful enough that they can only be granted directly
// in the database (e.g. via migration), never through the API.
private val DB_ONLY_PERMISSIONS = setOf(
    AdminPermissions.CREATE_ADMIN,
    AdminPermissions.DELETE_ADMIN,
    AdminPermissions.ASSIGN_ADMIN_ROLES,
)

@Service
class AdminRoleService(
    private val adminRoleRepository: AdminRoleRepository,
    private val permissionRepository: AdminRolePermissionRepository,
) {

    fun listAll(): List<AdminRoleWithPermissions> {
        return adminRoleRepository.findAll().map {
            AdminRoleWithPermissions(it, permissionsForRole(it.id))
        }
    }

    @Transactional
    fun createRole(upload: UploadAdminRole): AdminRoleWithPermissions {
        rejectDbOnlyPermissions(upload.permissions)
        val role = AdminRole(name = upload.name)
        adminRoleRepository.save(role)
        val permissions = upload.permissions.distinct()
        permissions.forEach { permissionRepository.save(AdminRolePermission(role = role, permission = it)) }
        return AdminRoleWithPermissions(role, permissions)
    }

    @Transactional
    fun updateRole(roleId: UUID, patch: PatchAdminRole): AdminRoleWithPermissions {
        patch.permissions?.let { rejectDbOnlyPermissions(it) }

        val role = adminRoleRepository.findById(roleId)
            .orElseThrow { EntityDoesNotExistException("Role $roleId does not exist") }

        patch.name?.let {
            role.name = it
            adminRoleRepository.save(role)
        }

        val permissions = if (patch.permissions != null) {
            permissionRepository.deleteAll(permissionRepository.findByRoleId(role.id))
            val newPermissions = patch.permissions.distinct()
            newPermissions.forEach { permissionRepository.save(AdminRolePermission(role = role, permission = it)) }
            newPermissions
        } else {
            permissionsForRole(role.id)
        }

        return AdminRoleWithPermissions(role, permissions)
    }

    @Transactional
    fun deleteRole(roleId: UUID) {
        val role = adminRoleRepository.findById(roleId)
            .orElseThrow { EntityDoesNotExistException("Role $roleId does not exist") }
        adminRoleRepository.delete(role)
    }

    private fun permissionsForRole(roleId: UUID): List<AdminPermissions> {
        return permissionRepository.findByRoleId(roleId).mapNotNull { it.permission }
    }

    private fun rejectDbOnlyPermissions(permissions: List<AdminPermissions>) {
        if (permissions.any { it in DB_ONLY_PERMISSIONS }) {
            throw APIErrorException(listOf("These permissions can only be set in the database"))
        }
    }
}
