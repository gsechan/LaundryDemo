package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin

import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminSession
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AdminSessionRepository: JpaRepository<AdminSession, UUID>{
    fun findByToken(token: String): List<AdminSession>
    fun deleteByToken(token: String)
}
