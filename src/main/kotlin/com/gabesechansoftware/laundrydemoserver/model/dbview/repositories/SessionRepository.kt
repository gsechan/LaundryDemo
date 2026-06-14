package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories

import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Session
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SessionRepository: JpaRepository<Session, UUID>{
    fun findByToken(token: String): List<Session>
    fun deleteByToken(token: String)

}