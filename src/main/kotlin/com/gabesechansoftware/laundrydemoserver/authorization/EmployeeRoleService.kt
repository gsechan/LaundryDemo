package com.gabesechansoftware.laundrydemoserver.authorization

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeRole
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeRolePermission
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee.EmployeeRolePermissionRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee.EmployeeRoleRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

data class UploadEmployeeRole(
    val name: String,
    val permissions: List<EmployeePermissions>,
)

data class PatchEmployeeRole(
    val name: String?,
    val permissions: List<EmployeePermissions>?,
)

data class EmployeeRoleWithPermissions(
    val role: EmployeeRole,
    val permissions: List<EmployeePermissions>,
)

@Service
class EmployeeRoleService(
    private val employeeRoleRepository: EmployeeRoleRepository,
    private val permissionRepository: EmployeeRolePermissionRepository,
) {

    fun listByOrganization(organizationId: UUID): List<EmployeeRoleWithPermissions> =
        employeeRoleRepository.findByOrganizationId(organizationId).map {
            EmployeeRoleWithPermissions(it, permissionsForRole(it.id))
        }

    @Transactional
    fun createRole(organizationId: UUID, upload: UploadEmployeeRole): EmployeeRoleWithPermissions {
        val role = EmployeeRole(name = upload.name, organizationId = organizationId)
        employeeRoleRepository.save(role)
        val permissions = upload.permissions.distinct()
        permissions.forEach {
            permissionRepository.save(EmployeeRolePermission(role = role, organizationId = organizationId, permission = it))
        }
        return EmployeeRoleWithPermissions(role, permissions)
    }

    @Transactional
    fun updateRole(organizationId: UUID, roleId: UUID, patch: PatchEmployeeRole): EmployeeRoleWithPermissions {
        val role = findInOrg(organizationId, roleId)

        patch.name?.let {
            role.name = it
            employeeRoleRepository.save(role)
        }

        val permissions = if (patch.permissions != null) {
            permissionRepository.deleteAll(permissionRepository.findByRoleId(role.id))
            val newPermissions = patch.permissions.distinct()
            newPermissions.forEach {
                permissionRepository.save(EmployeeRolePermission(role = role, organizationId = organizationId, permission = it))
            }
            newPermissions
        } else {
            permissionsForRole(role.id)
        }

        return EmployeeRoleWithPermissions(role, permissions)
    }

    @Transactional
    fun deleteRole(organizationId: UUID, roleId: UUID) {
        val role = findInOrg(organizationId, roleId)
        employeeRoleRepository.delete(role)
    }

    private fun permissionsForRole(roleId: UUID): List<EmployeePermissions> =
        permissionRepository.findByRoleId(roleId).mapNotNull { it.permission }

    private fun findInOrg(organizationId: UUID, roleId: UUID): EmployeeRole {
        val role = employeeRoleRepository.findById(roleId)
            .orElseThrow { EntityDoesNotExistException("Role $roleId does not exist") }
        if (role.organizationId != organizationId) {
            throw EntityDoesNotExistException("Role $roleId does not exist in organization $organizationId")
        }
        return role
    }
}
