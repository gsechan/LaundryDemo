package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin

import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRolePermission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AdminRolePermissionRepository: JpaRepository<AdminRolePermission, UUID> {
    fun findByRoleId(roleId: UUID): List<AdminRolePermission>

    @Query("""
        SELECT DISTINCT p.permission FROM AdminRolePermission p
        WHERE p.role.id IN (
            SELECT m.role.id FROM AdminRoleMembership m WHERE m.admin.id = :adminId
        )
    """)
    fun findPermissionsByAdminId(@Param("adminId") adminId: UUID): List<AdminPermissions>
}
