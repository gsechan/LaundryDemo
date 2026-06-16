package com.gabesechansoftware.laundrydemoserver.authorization

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeRoleMembership
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee.EmployeeRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee.EmployeeRoleMembershipRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee.EmployeeRoleRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EmployeeRoleMembershipService(
    private val employeeRepository: EmployeeRepository,
    private val employeeRoleRepository: EmployeeRoleRepository,
    private val membershipRepository: EmployeeRoleMembershipRepository,
) {

    fun listByEmployee(employeeId: UUID): List<EmployeeRoleMembership> =
        membershipRepository.findByEmployeeId(employeeId)

    @Transactional
    fun assignRole(employeeId: UUID, roleId: UUID, locationId: UUID?): EmployeeRoleMembership {
        val employee = employeeRepository.findById(employeeId)
            .orElseThrow { EntityDoesNotExistException("Employee $employeeId does not exist") }
        val role = employeeRoleRepository.findById(roleId)
            .orElseThrow { EntityDoesNotExistException("Role $roleId does not exist") }

        if (employee.organizationId != role.organizationId) {
            throw APIErrorException(listOf("Role does not belong to the same organization as the employee"))
        }
        if (membershipRepository.existsByEmployeeIdAndRoleId(employeeId, roleId)) {
            throw APIErrorException(listOf("Employee already has this role"))
        }

        val membership = EmployeeRoleMembership(employee = employee, role = role, locationId = locationId)
        membershipRepository.save(membership)
        return membership
    }

    @Transactional
    fun removeMembership(membershipId: UUID) {
        val membership = membershipRepository.findById(membershipId)
            .orElseThrow { EntityDoesNotExistException("Role membership $membershipId does not exist") }
        membershipRepository.delete(membership)
    }
}
