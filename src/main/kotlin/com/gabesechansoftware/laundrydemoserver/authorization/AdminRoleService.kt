package com.gabesechansoftware.laundrydemoserver.authorization

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
        val role = AdminRole(name = upload.name)
        adminRoleRepository.save(role)
        val permissions = upload.permissions.distinct()
        permissions.forEach { permissionRepository.save(AdminRolePermission(role = role, permission = it)) }
        return AdminRoleWithPermissions(role, permissions)
    }

    @Transactional
    fun updateRole(roleId: UUID, patch: PatchAdminRole): AdminRoleWithPermissions {
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
}
