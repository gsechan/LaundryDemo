package com.gabesechansoftware.laundrydemoserver.employees

import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.Employee
import org.springframework.stereotype.Component

@Component
class EmployeeViewMapper {
    fun toView(employee: Employee) = EmployeeView(
        id = employee.id.toString(),
        name = employee.name,
        email = employee.email,
        phone = employee.phone,
        organizationId = employee.organizationId?.toString(),
    )
}
