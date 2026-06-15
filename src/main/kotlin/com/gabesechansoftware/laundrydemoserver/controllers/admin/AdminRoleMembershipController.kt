package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.authentication.AuthenticatedAdmin
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.authorization.AdminRoleMembershipService
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRoleMembership
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


data class CreateMembershipRequest(
    val adminId: UUID,
    val roleId: UUID,
)

data class MembershipView(
    val id: String,
    val adminId: String,
    val roleId: String,
)

fun AdminRoleMembership.toView() =
    MembershipView(id.toString(), admin!!.id.toString(), role!!.id.toString())


@RestController
class AdminRoleMembershipController(
    private val membershipService: AdminRoleMembershipService,
    private val adminAuthorizationService: AdminAuthorizationService,
) {

    private fun canAssign(admin: Admin) =
        adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.ASSIGN_ADMIN_ROLES), admin)

    @PostMapping("/admin/role-memberships")
    fun assignRole(
        @RequestBody request: CreateMembershipRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<MembershipView> {
        if (!canAssign(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to manage role memberships")
        }
        val membership = membershipService.assignRole(request.adminId, request.roleId)
        return NetworkResponse(membership.toView())
    }

    @DeleteMapping("/admin/role-memberships/{id}")
    fun removeMembership(
        @PathVariable id: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<Unit> {
        if (!canAssign(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to manage role memberships")
        }
        membershipService.removeMembership(id)
        return NetworkResponse(Unit)
    }
}
