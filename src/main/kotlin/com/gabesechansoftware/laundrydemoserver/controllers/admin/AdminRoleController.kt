package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.authentication.AuthenticatedAdmin
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.authorization.AdminRoleService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminRoleWithPermissions
import com.gabesechansoftware.laundrydemoserver.authorization.PatchAdminRole
import com.gabesechansoftware.laundrydemoserver.authorization.UploadAdminRole
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


data class AdminRoleView(
    val id: String,
    val name: String?,
    val permissions: List<String>,
)

fun AdminRoleWithPermissions.toView() =
    AdminRoleView(role.id.toString(), role.name, permissions.map { it.toString() })

data class CreateRoleRequest(val role: UploadAdminRole)
data class PatchRoleRequest(val role: PatchAdminRole)


@RestController
class AdminRoleController(
    private val adminRoleService: AdminRoleService,
    private val adminAuthorizationService: AdminAuthorizationService,
) {

    private fun canAssign(admin: Admin) =
        adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.ASSIGN_ADMIN_ROLES), admin)

    @GetMapping("/admin/roles")
    fun listRoles(
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<List<AdminRoleView>> {
        if (!canAssign(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to manage roles")
        }
        return NetworkResponse(adminRoleService.listAll().map { it.toView() })
    }

    @PostMapping("/admin/roles")
    fun createRole(
        @RequestBody request: CreateRoleRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<AdminRoleView> {
        if (!canAssign(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to manage roles")
        }
        return NetworkResponse(adminRoleService.createRole(request.role).toView())
    }

    @PatchMapping("/admin/roles/{id}")
    fun updateRole(
        @PathVariable id: UUID,
        @RequestBody request: PatchRoleRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<AdminRoleView> {
        if (!canAssign(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to manage roles")
        }
        return NetworkResponse(adminRoleService.updateRole(id, request.role).toView())
    }

    @DeleteMapping("/admin/roles/{id}")
    fun deleteRole(
        @PathVariable id: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<Unit> {
        if (!canAssign(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to manage roles")
        }
        adminRoleService.deleteRole(id)
        return NetworkResponse(Unit)
    }
}
