package com.gabesechansoftware.laundrydemoserver.auth

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.DatabaseDataInvalidException
import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Password
import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Session
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.PasswordRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.SessionRepository
import com.gabesechansoftware.laundrydemoserver.model.validation.PasswordValidator
import jakarta.transaction.Transactional
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

@Component
class LoginAuthenticator(
    private val passwordRepo: PasswordRepository,
    private val sessionRepository: SessionRepository,
    private val encoder: PasswordEncoder = BCryptPasswordEncoder(16),
    private val timeSource: TimeSource = TimeSource(),
    private val passwordValidator: PasswordValidator = PasswordValidator(),
    ) {

    fun authenticatePassword(org:UUID, phone: String, unhashed: String): User {
        val password = findPossiblePassword(org, phone)
        if(!encoder.matches(unhashed, password.hash)) {
            throw BadLoginException()
        }
        return password.user!!
    }

    fun createSession(user: User): Session {
        val token = UUID.randomUUID().toString()
        val expireAt = timeSource.now().plusYears(1)
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
            sessionRepository.deleteByToken(token)
            throw BadLoginException()
        }
        //Using a token refreshes expiration
        updateExpiration(session, timeSource.now().plusYears(1))
        return session.user!!
    }

    @Transactional
    fun logout(token: String) {
        sessionRepository.deleteByToken(token)
    }


    private fun getSessionForToken(token: String): Session {
        val sessions = sessionRepository.findByToken(token)
        if(sessions.size > 1) {
            sessionRepository.deleteByToken(token)
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

    fun createPasswordForUser(user: User, newPassword: String) {
        val errors = mutableListOf<String>()
        val password = Password().apply {
            this.user = user
            this.hash = encoder.encode(newPassword)
        }
        passwordValidator.validatePassword(newPassword, errors)
        if(errors.isNotEmpty()) {
            throw APIErrorException(errors)
        }
        passwordRepo.save(password)
    }

    fun updatePasswordForUser(user: User, newPassword: String) {
        val errors = mutableListOf<String>()
        passwordValidator.validatePassword(newPassword, errors)

        val password = passwordRepo.findByOrganizationIdAndPhone(user.organization!!.id, user.phone!!)
        if(password == null) {
            throw DatabaseDataInvalidException("User does not have a password")
        }
        password.hash = encoder.encode(newPassword)
        if(errors.isNotEmpty()) {
            throw APIErrorException(errors)
        }
        passwordRepo.save(password)

    }
}