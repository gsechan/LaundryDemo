package com.gabesechansoftware.laundrydemoserver.auth

import com.gabesechansoftware.laundrydemoserver.DataConstraintException
import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Session
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.SessionRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.UserRepository
import com.gabesechansoftware.laundrydemoserver.services.PasswordService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

data class UserSession(val user: User, val token: String)

@Component
class LoginAuthenticator(
    private val passwordService: PasswordService,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
) {

    private val encoder = BCryptPasswordEncoder(16)

    fun authenticateLoginAndCreateSession(org:UUID, phone: String, unhashed: String): UserSession {
        val password = passwordService.findPossiblePassword(org, phone)
        val matches = encoder.matches(unhashed, password.hash)

        if(matches) {
            val token = UUID.randomUUID().toString()
            val expire = OffsetDateTime.now().plusYears(1)
            addSession(password.user!!.id, token, expire)
            return UserSession(password.user!!, token)
        }
        else {
            throw BadLoginException()
        }
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
            throw DataConstraintException("More than one session exists with token $token")
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

    fun addSession(userId: UUID, newToken: String, expireAt: OffsetDateTime) {
        val userRef = userRepository.getReferenceById(userId)
        val session = Session().apply {
            token = newToken
            user = userRef
            expiration = expireAt
        }
        sessionRepository.save(session)
    }

}