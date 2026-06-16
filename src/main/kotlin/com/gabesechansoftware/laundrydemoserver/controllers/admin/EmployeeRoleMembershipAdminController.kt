package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.authentication.AuthenticatedAdmin
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.authorization.EmployeeRoleMembershipService
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeRoleMembership
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class CreateEmployeeMembershipRequest(
    val employeeId: UUID,
    val roleId: UUID,
    val locationId: UUID?,
)

data class EmployeeMembershipView(
    val id: String,
    val employeeId: String,
    val roleId: String,
    val locationId: String?,
)

fun EmployeeRoleMembership.toView() = EmployeeMembershipView(
    id = id.toString(),
    employeeId = employee!!.id.toString(),
    roleId = role!!.id.toString(),
    locationId = locationId?.toString(),
)

@RestController
class EmployeeRoleMembershipAdminController(
    private val membershipService: EmployeeRoleMembershipService,
    private val adminAuthorizationService: AdminAuthorizationService,
) {

    private fun canManage(admin: Admin) =
        adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.EDIT_ORG), admin)

    @GetMapping("/admin/organizations/{orgId}/employee-role-memberships")
    fun listMemberships(
        @PathVariable orgId: UUID,
        @RequestParam employeeId: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<List<EmployeeMembershipView>> {
        if (!canManage(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to manage employee role memberships")
        }
        return NetworkResponse(membershipService.listByEmployee(employeeId).map { it.toView() })
    }

    @PostMapping("/admin/organizations/{orgId}/employee-role-memberships")
    fun assignRole(
        @PathVariable orgId: UUID,
        @RequestBody request: CreateEmployeeMembershipRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<EmployeeMembershipView> {
        if (!canManage(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to manage employee role memberships")
        }
        val membership = membershipService.assignRole(request.employeeId, request.roleId, request.locationId)
        return NetworkResponse(membership.toView())
    }

    @DeleteMapping("/admin/organizations/{orgId}/employee-role-memberships/{id}")
    fun removeMembership(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<Unit> {
        if (!canManage(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to manage employee role memberships")
        }
        membershipService.removeMembership(id)
        return NetworkResponse(Unit)
    }
}
