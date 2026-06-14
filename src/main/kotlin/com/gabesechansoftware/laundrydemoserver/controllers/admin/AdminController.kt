package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.admins.AdminService
import com.gabesechansoftware.laundrydemoserver.admins.UploadAdmin
import com.gabesechansoftware.laundrydemoserver.authentication.AuthenticatedAdmin
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.controllers.auth.AdminView
import com.gabesechansoftware.laundrydemoserver.controllers.auth.toView
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


data class CreateAdminRequest(
    val admin: UploadAdmin,
    val password: String,
)


@RestController
class AdminController(
    private val adminService: AdminService,
    private val adminAuthorizationService: AdminAuthorizationService,
) {

    @PostMapping("/admin/admins")
    fun createAdmin(
        @RequestBody request: CreateAdminRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<AdminView> {
        if (!adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.CREATE_ADMIN), authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to create admins")
        }
        val admin = adminService.createAdmin(request.admin, request.password)
        return NetworkResponse(admin.toView())
    }
}
