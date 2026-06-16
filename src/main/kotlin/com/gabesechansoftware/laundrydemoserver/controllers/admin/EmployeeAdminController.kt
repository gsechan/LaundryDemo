package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.authentication.AuthenticatedAdmin
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.employees.EmployeeService
import com.gabesechansoftware.laundrydemoserver.employees.EmployeeView
import com.gabesechansoftware.laundrydemoserver.employees.EmployeeViewMapper
import com.gabesechansoftware.laundrydemoserver.employees.PatchEmployee
import com.gabesechansoftware.laundrydemoserver.employees.UploadEmployee
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class CreateEmployeeRequest(
    val employee: UploadEmployee,
    val password: String,
)

data class PatchEmployeeRequest(
    val employee: PatchEmployee,
)

@RestController
class EmployeeAdminController(
    private val employeeService: EmployeeService,
    private val employeeViewMapper: EmployeeViewMapper,
    private val adminAuthorizationService: AdminAuthorizationService,
) {

    private fun canCreate(admin: Admin) =
        adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.EDIT_ORG), admin)

    private fun canDelete(admin: Admin) =
        adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.EDIT_ORG), admin)

    @GetMapping("/admin/organizations/{orgId}/employees")
    fun listEmployees(
        @PathVariable orgId: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<List<EmployeeView>> {
        if (!adminAuthorizationService.permissionsCheckAny(
                listOf(AdminPermissions.EDIT_ORG, AdminPermissions.CREATE_ORG),
                authedAdmin
            )
        ) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to view employees")
        }
        return NetworkResponse(employeeService.listByOrganization(orgId).map { employeeViewMapper.toView(it) })
    }

    @PostMapping("/admin/organizations/{orgId}/employees")
    fun createEmployee(
        @PathVariable orgId: UUID,
        @RequestBody request: CreateEmployeeRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<EmployeeView> {
        if (!canCreate(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to create employees")
        }
        val employee = employeeService.createEmployee(orgId, request.employee, request.password)
        return NetworkResponse(employeeViewMapper.toView(employee))
    }

    @PatchMapping("/admin/organizations/{orgId}/employees/{id}")
    fun updateEmployee(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: PatchEmployeeRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<EmployeeView> {
        if (!canCreate(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to edit employees")
        }
        val employee = employeeService.updateEmployee(orgId, id, request.employee)
        return NetworkResponse(employeeViewMapper.toView(employee))
    }

    @DeleteMapping("/admin/organizations/{orgId}/employees/{id}")
    fun deleteEmployee(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<Unit> {
        if (!canDelete(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to delete employees")
        }
        employeeService.deleteEmployee(orgId, id)
        return NetworkResponse(Unit)
    }
}
