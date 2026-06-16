package com.gabesechansoftware.laundrydemoserver.authorization

import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.Employee
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee.EmployeeRolePermissionRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EmployeeAuthorizationService(
    private val permissionRepository: EmployeeRolePermissionRepository,
) {

    fun permissionsCheckAll(permissions: List<EmployeePermissions>, employee: Employee, locationId: UUID? = null): Boolean {
        if (permissions.isEmpty()) return true
        val held = permissionsFor(employee, locationId).toSet()
        return held.containsAll(permissions)
    }

    fun permissionsCheckAny(permissions: List<EmployeePermissions>, employee: Employee, locationId: UUID? = null): Boolean {
        if (permissions.isEmpty()) return true
        val held = permissionsFor(employee, locationId).toSet()
        return permissions.any { it in held }
    }

    fun permissionsFor(employee: Employee, locationId: UUID? = null): List<EmployeePermissions> =
        if (locationId != null) {
            permissionRepository.findPermissionsByEmployeeIdAndLocationId(employee.id, locationId)
        } else {
            permissionRepository.findOrgWidePermissionsByEmployeeId(employee.id)
        }
}
