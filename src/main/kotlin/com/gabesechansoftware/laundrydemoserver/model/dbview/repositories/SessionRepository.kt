package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories

import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Session
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface SessionRepository: JpaRepository<Session, UUID>{
    fun findByToken(token: String): List<Session>
    fun deleteByToken(token: String)

    @Modifying
    @Query("DELETE FROM Session s WHERE s.user.organization.id = :organizationId")
    fun deleteByOrganizationId(@Param("organizationId") organizationId: UUID)
}