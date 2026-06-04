package com.gabesechansoftware.laundrydemoserver.repositories

import com.gabesechansoftware.laundrydemoserver.model.Organization
import com.gabesechansoftware.laundrydemoserver.model.User
import com.gabesechansoftware.laundrydemoserver.model.auth.Password
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

data class UserAndPassword(val user: User, val password: Password)

@Repository
interface PasswordRepository: JpaRepository<Password, Long>{
    @Query("""
        SELECT p FROM Password p, User u
        WHERE p.user.phone = :phone AND p.user.organization.id = :organizationId
    """)
    fun findByOrganizationIdAndPhone(
        @Param("organizationId") organizationId: UUID,
        @Param("phone") phone: String,
    ): Password?
}