package com.gabesechansoftware.laundrydemoserver.authentication

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.DatabaseDataInvalidException
import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.Employee
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeePassword
import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeeSession
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee.EmployeePasswordRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee.EmployeeSessionRepository
import com.gabesechansoftware.laundrydemoserver.model.validation.PasswordValidator
import jakarta.transaction.Transactional
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class EmployeeLoginAuthenticator(
    private val passwordRepo: EmployeePasswordRepository,
    private val sessionRepository: EmployeeSessionRepository,
    private val encoder: PasswordEncoder = BCryptPasswordEncoder(16),
    private val timeSource: TimeSource = TimeSource(),
    private val passwordValidator: PasswordValidator = PasswordValidator(),
) {

    fun authenticatePassword(phone: String, unhashed: String): Employee {
        val passwords = passwordRepo.findByEmployeePhone(phone)
        if (passwords.isEmpty()) throw BadLoginException()
        val matched = passwords.firstOrNull { encoder.matches(unhashed, it.hash) }
            ?: throw BadLoginException()
        return matched.employee!!
    }

    fun createSession(employee: Employee): EmployeeSession {
        val token = UUID.randomUUID().toString()
        val expireAt = timeSource.now().plusHours(1)
        val session = EmployeeSession().apply {
            this.token = token
            this.employee = employee
            expiration = expireAt
        }
        sessionRepository.save(session)
        return session
    }

    fun authenticateToken(token: String): Employee {
        val session = getSessionForToken(token)
        if (session.expiration!!.isBefore(timeSource.now())) {
            sessionRepository.deleteByToken(token)
            throw BadLoginException()
        }
        updateExpiration(session, timeSource.now().plusHours(1))
        return session.employee!!
    }

    @Transactional
    fun logout(token: String) {
        sessionRepository.deleteByToken(token)
    }

    fun createPasswordForEmployee(employee: Employee, newPassword: String) {
        val errors = mutableListOf<String>()
        passwordValidator.validatePassword(newPassword, errors)
        if (errors.isNotEmpty()) throw APIErrorException(errors)
        val password = EmployeePassword().apply {
            this.employee = employee
            this.hash = encoder.encode(newPassword)
        }
        passwordRepo.save(password)
    }

    fun updatePasswordForEmployee(employee: Employee, newPassword: String) {
        val errors = mutableListOf<String>()
        passwordValidator.validatePassword(newPassword, errors)
        if (errors.isNotEmpty()) throw APIErrorException(errors)
        val password = passwordRepo.findByEmployeeId(employee.id)
            ?: throw DatabaseDataInvalidException("Employee does not have a password")
        password.hash = encoder.encode(newPassword)
        passwordRepo.save(password)
    }

    private fun getSessionForToken(token: String): EmployeeSession {
        val sessions = sessionRepository.findByToken(token)
        if (sessions.size > 1) {
            sessionRepository.deleteByToken(token)
            throw DatabaseDataInvalidException("More than one session exists with token $token")
        }
        if (sessions.isEmpty()) throw BadAuthTokenException(token)
        return sessions[0]
    }

    private fun updateExpiration(session: EmployeeSession, expiration: OffsetDateTime) {
        session.expiration = expiration
        sessionRepository.save(session)
    }
}
