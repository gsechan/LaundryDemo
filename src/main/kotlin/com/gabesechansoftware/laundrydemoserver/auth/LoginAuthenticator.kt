package com.gabesechansoftware.laundrydemoserver.auth

import com.gabesechansoftware.laundrydemoserver.DatabaseDataInvalidException
import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Password
import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Session
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.PasswordRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.SessionRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

data class UserSession(val user: User, val token: String)

@Component
class LoginAuthenticator(
    private val passwordRepo: PasswordRepository,
    private val sessionRepository: SessionRepository,
) {

    private val encoder = BCryptPasswordEncoder(16)

    fun authenticatePassword(org:UUID, phone: String, unhashed: String): User {
        val password = findPossiblePassword(org, phone)
        if(!encoder.matches(unhashed, password.hash)) {
            throw BadLoginException()
        }
        return password.user!!
    }

    fun createSession(user: User): Session {
        val token = UUID.randomUUID().toString()
        val expireAt = OffsetDateTime.now().plusYears(1)
        val session = Session().apply {
            this.token = token
            this.user = user
            expiration = expireAt
        }
        sessionRepository.save(session)
        return session
    }

    fun authenticateToken(token: String): User {
        val session = getSessionForToken(token)
        if(session.expiration!!.toInstant().toEpochMilli() < Instant.now().toEpochMilli()) {
            throw BadLoginException()
        }
        //Using a token refreshes expiration
        updateExpiration(session, OffsetDateTime.now().plusYears(1))
        return session.user!!
    }

    fun logout(token: String) {
        sessionRepository.delete(getSessionForToken(token))
    }


    private fun getSessionForToken(token: String): Session {
        val sessions = sessionRepository.findByToken(token)
        if(sessions.size > 1) {
            throw DatabaseDataInvalidException("More than one session exists with token $token")
        }
        if(sessions.isEmpty()) {
            throw BadAuthTokenException(token)
        }
        return sessions[0]
    }

    private fun updateExpiration(session: Session, expiration: OffsetDateTime) {
        session.expiration = expiration
        sessionRepository.save(session)
    }

    private fun findPossiblePassword(org: UUID, phone: String): Password {
        return passwordRepo.findByOrganizationIdAndPhone(org, phone) ?: throw BadLoginException()
    }

    fun setPasswordForUser(user: User, password: String) {
        val password = Password().apply {
            this.user = user
            this.hash = encoder.encode(password)
        }
        passwordRepo.save(password)
    }

}