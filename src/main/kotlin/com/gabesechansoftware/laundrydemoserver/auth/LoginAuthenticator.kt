package com.gabesechansoftware.laundrydemoserver.auth

import com.gabesechansoftware.laundrydemoserver.model.User
import com.gabesechansoftware.laundrydemoserver.model.auth.Password
import com.gabesechansoftware.laundrydemoserver.repositories.UserRepository
import com.gabesechansoftware.laundrydemoserver.services.OrganizationService
import com.gabesechansoftware.laundrydemoserver.services.PasswordService
import com.gabesechansoftware.laundrydemoserver.services.SessionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

data class UserSession(val user: User, val token: String)

@Component
class LoginAuthenticator(
    @Autowired val passwordService: PasswordService,
    @Autowired val sessionService: SessionService
) {

    private val encoder = BCryptPasswordEncoder(16)

    fun authenticateLoginAndCreateSession(org:UUID, phone: String, unhashed: String): UserSession {
        val password = passwordService.findPossiblePassword(org, phone)
        val matches = encoder.matches(unhashed, password.hash)

        if(matches) {
            val token = UUID.randomUUID().toString()
            val expire = OffsetDateTime.now().plusYears(1)
            sessionService.addSesseion(password.user!!.id!!, token, expire)
            return UserSession(password.user!!, token)
        }
        else {
            throw BadLoginException()
        }
    }

    fun authenticateToken(token: String): User {
        val session = sessionService.getSessionForToken(token)
        //Using a token refreshes expiration
        sessionService.updateExpiration(session, OffsetDateTime.now().plusYears(1))
        return session.user!!
    }
}