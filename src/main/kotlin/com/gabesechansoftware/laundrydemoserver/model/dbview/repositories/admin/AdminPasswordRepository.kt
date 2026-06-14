package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin

import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminPassword
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AdminPasswordRepository: JpaRepository<AdminPassword, UUID>{
    @Query("""
        SELECT p FROM AdminPassword p
        WHERE p.admin.email = :email
    """)
    fun findByEmail(
        @Param("email") email: String,
    ): AdminPassword?
}
