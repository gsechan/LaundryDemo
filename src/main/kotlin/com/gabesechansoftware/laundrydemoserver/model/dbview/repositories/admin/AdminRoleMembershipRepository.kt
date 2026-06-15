package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin

import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRoleMembership
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AdminRoleMembershipRepository: JpaRepository<AdminRoleMembership, UUID> {
    fun findByAdminId(adminId: UUID): List<AdminRoleMembership>
    fun existsByAdminIdAndRoleId(adminId: UUID, roleId: UUID): Boolean
}
