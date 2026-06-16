package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.authentication.AuthenticatedAdmin
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.authorization.EmployeePermissions
import com.gabesechansoftware.laundrydemoserver.authorization.EmployeeRoleService
import com.gabesechansoftware.laundrydemoserver.authorization.EmployeeRoleWithPermissions
import com.gabesechansoftware.laundrydemoserver.authorization.PatchEmployeeRole
import com.gabesechansoftware.laundrydemoserver.authorization.UploadEmployeeRole
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class EmployeeRoleView(
    val id: String,
    val name: String?,
    val organizationId: String?,
    val permissions: List<String>,
)

fun EmployeeRoleWithPermissions.toView() =
    EmployeeRoleView(
        id = role.id.toString(),
        name = role.name,
        organizationId = role.organizationId?.toString(),
        permissions = permissions.map { it.toString() },
    )

data class CreateEmployeeRoleRequest(val role: UploadEmployeeRole)
data class PatchEmployeeRoleRequest(val role: PatchEmployeeRole)

@RestController
class EmployeeRoleAdminController(
    private val employeeRoleService: EmployeeRoleService,
    private val adminAuthorizationService: AdminAuthorizationService,
) {

    private fun canManage(admin: Admin) =
        adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.EDIT_ORG), admin)

    @GetMapping("/admin/organizations/{orgId}/employee-roles")
    fun listRoles(
        @PathVariable orgId: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<List<EmployeeRoleView>> {
        if (!canManage(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to manage employee roles")
        }
        return NetworkResponse(employeeRoleService.listByOrganization(orgId).map { it.toView() })
    }

    @PostMapping("/admin/organizations/{orgId}/employee-roles")
    fun createRole(
        @PathVariable orgId: UUID,
        @RequestBody request: CreateEmployeeRoleRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<EmployeeRoleView> {
        if (!canManage(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to manage employee roles")
        }
        return NetworkResponse(employeeRoleService.createRole(orgId, request.role).toView())
    }

    @PatchMapping("/admin/organizations/{orgId}/employee-roles/{id}")
    fun updateRole(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: PatchEmployeeRoleRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<EmployeeRoleView> {
        if (!canManage(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to manage employee roles")
        }
        return NetworkResponse(employeeRoleService.updateRole(orgId, id, request.role).toView())
    }

    @DeleteMapping("/admin/organizations/{orgId}/employee-roles/{id}")
    fun deleteRole(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<Unit> {
        if (!canManage(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to manage employee roles")
        }
        employeeRoleService.deleteRole(orgId, id)
        return NetworkResponse(Unit)
    }
}
