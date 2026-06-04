package com.gabesechansoftware.laundrydemoserver.repositories

import com.gabesechansoftware.laundrydemoserver.model.auth.Session
import org.springframework.data.jpa.repository.JpaRepository


interface SessionRepository: JpaRepository<Session, Long>{
    fun findByToken(token: String): List<Session>

}