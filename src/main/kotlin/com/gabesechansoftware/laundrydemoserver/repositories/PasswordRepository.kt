package com.gabesechansoftware.laundrydemoserver.repositories

import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Password
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface PasswordRepository: JpaRepository<Password, UUID>{
    @Query("""
        SELECT p FROM Password p, User u
        WHERE p.user.phone = :phone AND p.user.organization.id = :organizationId
    """)
    fun findByOrganizationIdAndPhone(
        @Param("organizationId") organizationId: UUID,
        @Param("phone") phone: String,
    ): Password?
}