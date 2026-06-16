package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee

import com.gabesechansoftware.laundrydemoserver.authorization.EmployeePermissions
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeRolePermission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface EmployeeRolePermissionRepository : JpaRepository<EmployeeRolePermission, UUID> {
    fun findByRoleId(roleId: UUID): List<EmployeeRolePermission>

    @Query("""
        SELECT DISTINCT p.permission FROM EmployeeRolePermission p
        WHERE p.role.id IN (
            SELECT m.role.id FROM EmployeeRoleMembership m
            WHERE m.employee.id = :employeeId
            AND (m.locationId IS NULL OR m.locationId = :locationId)
        )
    """)
    fun findPermissionsByEmployeeIdAndLocationId(
        @Param("employeeId") employeeId: UUID,
        @Param("locationId") locationId: UUID,
    ): List<EmployeePermissions>

    @Query("""
        SELECT DISTINCT p.permission FROM EmployeeRolePermission p
        WHERE p.role.id IN (
            SELECT m.role.id FROM EmployeeRoleMembership m
            WHERE m.employee.id = :employeeId
            AND m.locationId IS NULL
        )
    """)
    fun findOrgWidePermissionsByEmployeeId(
        @Param("employeeId") employeeId: UUID,
    ): List<EmployeePermissions>
}
