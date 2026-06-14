package com.gabesechansoftware.laundrydemoserver.authorization

import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRolePermissionRepository
import org.springframework.stereotype.Service

@Service
class AdminAuthorizationService(
    private val permissionRepository: AdminRolePermissionRepository,
) {

    /** True if the admin holds every one of the given permissions. */
    fun permissionsCheckAll(permissions: List<AdminPermissions>, admin: Admin): Boolean {
        if (permissions.isEmpty()) {
            return true
        }
        val held = permissionsForAdmin(admin)
        return held.containsAll(permissions)
    }

    /** True if the admin holds at least one of the given permissions. */
    fun permissionsCheckAny(permissions: List<AdminPermissions>, admin: Admin): Boolean {
        if (permissions.isEmpty()) {
            return true
        }
        val held = permissionsForAdmin(admin)
        return permissions.any { it in held }
    }

    private fun permissionsForAdmin(admin: Admin): Set<AdminPermissions> {
        return permissionRepository.findPermissionsByAdminId(admin.id).toSet()
    }
}
