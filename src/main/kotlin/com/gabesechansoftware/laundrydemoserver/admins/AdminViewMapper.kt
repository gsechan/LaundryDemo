package com.gabesechansoftware.laundrydemoserver.admins

import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRoleMembershipRepository
import org.springframework.stereotype.Component

@Component
class AdminViewMapper(
    private val adminAuthorizationService: AdminAuthorizationService,
    private val membershipRepository: AdminRoleMembershipRepository,
) {

    fun toView(admin: Admin): AdminView {
        val permissions = adminAuthorizationService.permissionsFor(admin).map { it.toString() }
        val roleMemberships = membershipRepository.findByAdminId(admin.id).map {
            RoleAssignmentView(it.id.toString(), it.role!!.id.toString())
        }
        return AdminView(
            id = admin.id.toString(),
            name = admin.name,
            email = admin.email,
            phone = admin.phone,
            permissions = permissions,
            roleMemberships = roleMemberships,
        )
    }
}
