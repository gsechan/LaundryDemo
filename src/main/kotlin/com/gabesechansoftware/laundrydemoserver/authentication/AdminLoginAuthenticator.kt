package com.gabesechansoftware.laundrydemoserver.authentication

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.DatabaseDataInvalidException
import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminPassword
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminSession
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminPasswordRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminSessionRepository
import com.gabesechansoftware.laundrydemoserver.model.validation.PasswordValidator
import jakarta.transaction.Transactional
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class AdminLoginAuthenticator(
    private val passwordRepo: AdminPasswordRepository,
    private val sessionRepository: AdminSessionRepository,
    private val encoder: PasswordEncoder = BCryptPasswordEncoder(16),
    private val timeSource: TimeSource = TimeSource(),
    private val passwordValidator: PasswordValidator = PasswordValidator(),
    ) {

    fun authenticatePassword(email: String, unhashed: String): Admin {
        val password = findPossiblePassword(email)
        if(!encoder.matches(unhashed, password.hash)) {
            throw BadLoginException()
        }
        return password.admin!!
    }

    fun createSession(admin: Admin): AdminSession {
        val token = UUID.randomUUID().toString()
        val expireAt = timeSource.now().plusHours(1)
        val session = AdminSession().apply {
            this.token = token
            this.admin = admin
            expiration = expireAt
        }
        sessionRepository.save(session)
        return session
    }

    fun authenticateToken(token: String): Admin {
        val session = getSessionForToken(token)
        if(session.expiration!!.isBefore(timeSource.now())) {
            sessionRepository.deleteByToken(token)
            throw BadLoginException()
        }
        //Using a token refreshes expiration
        updateExpiration(session, timeSource.now().plusHours(1))
        return session.admin!!
    }

    @Transactional
    fun logout(token: String) {
        sessionRepository.deleteByToken(token)
    }


    private fun getSessionForToken(token: String): AdminSession {
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

    private fun updateExpiration(session: AdminSession, expiration: OffsetDateTime) {
        session.expiration = expiration
        sessionRepository.save(session)
    }

    private fun findPossiblePassword(email: String): AdminPassword {
        return passwordRepo.findByEmail(email) ?: throw BadLoginException()
    }

    fun createPasswordForAdmin(admin: Admin, newPassword: String) {
        val errors = mutableListOf<String>()
        val password = AdminPassword().apply {
            this.admin = admin
            this.hash = encoder.encode(newPassword)
        }
        passwordValidator.validatePassword(newPassword, errors)
        if(errors.isNotEmpty()) {
            throw APIErrorException(errors)
        }
        passwordRepo.save(password)
    }

    fun updatePasswordForAdmin(admin: Admin, newPassword: String) {
        val errors = mutableListOf<String>()
        passwordValidator.validatePassword(newPassword, errors)

        val password = passwordRepo.findByEmail(admin.email!!)
            ?: throw DatabaseDataInvalidException("Admin does not have a password")
        password.hash = encoder.encode(newPassword)
        if(errors.isNotEmpty()) {
            throw APIErrorException(errors)
        }
        passwordRepo.save(password)

    }
}
