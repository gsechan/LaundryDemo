package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin

import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRolePermission
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AdminRolePermissionRepository: JpaRepository<AdminRolePermission, UUID> {
    fun findByRoleId(roleId: UUID): List<AdminRolePermission>
    fun findByRoleIdIn(roleIds: Collection<UUID>): List<AdminRolePermission>
}
