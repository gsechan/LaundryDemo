package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.authentication.AuthenticatedAdmin
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.users.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


data class AdminUserView(
    val id: String,
    val name: String?,
    val email: String?,
    val phone: String?,
)

fun User.toAdminUserView() = AdminUserView(id.toString(), name, email, phone)


@RestController
class AdminUserController(
    private val userService: UserService,
) {

    @GetMapping("/admin/organizations/{orgId}/users")
    fun listUsers(
        @PathVariable orgId: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<List<AdminUserView>> {
        return NetworkResponse(userService.listByOrganization(orgId).map { it.toAdminUserView() })
    }
}
