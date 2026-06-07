package com.gabesechansoftware.laundrydemoserver.services

import com.gabesechansoftware.laundrydemoserver.auth.BadAuthTokenException
import com.gabesechansoftware.laundrydemoserver.model.user.User
import com.gabesechansoftware.laundrydemoserver.model.auth.Session
import com.gabesechansoftware.laundrydemoserver.repositories.SessionRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Transactional
class SessionService(
    @Autowired private val sessionRepository: SessionRepository,
    @PersistenceContext private val entityManager: EntityManager
) {

    fun addSesseion(userId: UUID, newToken: String, expireAt: OffsetDateTime) {
        val userRef = entityManager.getReference(User::class.java, userId)
        val session = Session().apply {
            token = newToken
            user = userRef
            expiration = expireAt
        }
        entityManager.persist(session)
    }

    fun getSessionForToken(token: String): Session {
        val sessions = sessionRepository.findByToken(token)
        if(sessions.size > 1) {
            throw RuntimeException("More than one session exists with token $token")
        }
        if(sessions.isEmpty()) {
            throw BadAuthTokenException(token)
        }
        return sessions[0]
    }

    fun updateExpiration(session: Session, expiration: OffsetDateTime) {
        session.expiration = expiration
        sessionRepository.save(session)
    }

}