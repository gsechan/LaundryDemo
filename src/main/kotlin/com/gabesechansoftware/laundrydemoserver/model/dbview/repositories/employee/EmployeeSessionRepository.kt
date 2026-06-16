package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee

import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeSession
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EmployeeSessionRepository : JpaRepository<EmployeeSession, UUID> {
    fun findByToken(token: String): List<EmployeeSession>
    fun deleteByToken(token: String)
}
