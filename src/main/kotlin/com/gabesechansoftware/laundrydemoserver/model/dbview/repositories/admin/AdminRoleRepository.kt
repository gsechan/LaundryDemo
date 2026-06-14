package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin

import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRole
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AdminRoleRepository: JpaRepository<AdminRole, UUID> {
    fun findByName(name: String): AdminRole?
}
