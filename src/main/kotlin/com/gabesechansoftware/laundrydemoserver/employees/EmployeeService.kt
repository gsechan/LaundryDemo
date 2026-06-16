package com.gabesechansoftware.laundrydemoserver.employees

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.authentication.EmployeeLoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.Employee
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee.EmployeeRepository
import com.gabesechansoftware.laundrydemoserver.model.validation.EmployeeValidator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

data class UploadEmployee(
    val name: String,
    val email: String,
    val phone: String,
)

data class PatchEmployee(
    val name: String?,
    val email: String?,
    val phone: String?,
)

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val employeeLoginAuthenticator: EmployeeLoginAuthenticator,
    private val employeeValidator: EmployeeValidator = EmployeeValidator(),
) {

    fun listByOrganization(organizationId: UUID): List<Employee> =
        employeeRepository.findByOrganizationId(organizationId)

    @Transactional
    fun createEmployee(organizationId: UUID, upload: UploadEmployee, password: String): Employee {
        val employee = Employee(
            name = upload.name,
            email = upload.email,
            phone = upload.phone,
            organizationId = organizationId,
        )
        val errors = mutableListOf<String>()
        employeeValidator.validateEmployee(employee, errors)
        if (errors.isNotEmpty()) throw APIErrorException(errors)
        employeeRepository.save(employee)
        employeeLoginAuthenticator.createPasswordForEmployee(employee, password)
        return employee
    }

    @Transactional
    fun updateEmployee(organizationId: UUID, employeeId: UUID, patch: PatchEmployee): Employee {
        val employee = findInOrg(organizationId, employeeId)
        patch.name?.let { employee.name = it }
        patch.email?.let { employee.email = it }
        patch.phone?.let { employee.phone = it }
        val errors = mutableListOf<String>()
        employeeValidator.validateEmployee(employee, errors)
        if (errors.isNotEmpty()) throw APIErrorException(errors)
        return employeeRepository.save(employee)
    }

    @Transactional
    fun deleteEmployee(organizationId: UUID, employeeId: UUID) {
        val employee = findInOrg(organizationId, employeeId)
        employeeRepository.delete(employee)
    }

    private fun findInOrg(organizationId: UUID, employeeId: UUID): Employee {
        val employee = employeeRepository.findById(employeeId)
            .orElseThrow { EntityDoesNotExistException("Employee $employeeId does not exist") }
        if (employee.organizationId != organizationId) {
            throw EntityDoesNotExistException("Employee $employeeId does not exist in organization $organizationId")
        }
        return employee
    }
}
