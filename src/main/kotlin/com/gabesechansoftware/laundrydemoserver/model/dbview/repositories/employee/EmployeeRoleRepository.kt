package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee

import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeRole
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EmployeeRoleRepository : JpaRepository<EmployeeRole, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<EmployeeRole>
}
