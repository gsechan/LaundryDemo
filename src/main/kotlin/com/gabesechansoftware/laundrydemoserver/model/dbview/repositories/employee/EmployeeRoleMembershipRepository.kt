package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee

import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeRoleMembership
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EmployeeRoleMembershipRepository : JpaRepository<EmployeeRoleMembership, UUID> {
    fun findByEmployeeId(employeeId: UUID): List<EmployeeRoleMembership>
    fun existsByEmployeeIdAndRoleId(employeeId: UUID, roleId: UUID): Boolean
}
