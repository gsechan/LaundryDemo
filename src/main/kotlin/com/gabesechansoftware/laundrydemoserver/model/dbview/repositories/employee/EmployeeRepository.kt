package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee

import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.Employee
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EmployeeRepository : JpaRepository<Employee, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<Employee>
}
