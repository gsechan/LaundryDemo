package com.gabesechansoftware.laundrydemoserver.auth

import com.gabesechansoftware.laundrydemoserver.model.User
import com.gabesechansoftware.laundrydemoserver.model.auth.Password
import com.gabesechansoftware.laundrydemoserver.repositories.UserRepository
import com.gabesechansoftware.laundrydemoserver.services.PasswordService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.stereotype.Component
import java.util.UUID

data class UserSession(val user: User, val token: String)

@Component
class LoginAuthenticator(
    @Autowired val passwordService: PasswordService
) {

    private val encoder = BCryptPasswordEncoder(16)

    fun authenticateLoginAndCreateSession(org:UUID, phone: String, unhashed: String): UserSession {
        val password = passwordService.findPossiblePassword(org, phone)
        val matches = encoder.matches(unhashed, password.hash)

        if(matches) {
            val token = UUID.randomUUID().toString()
            return UserSession(password.user!!, token)
        }
        else {
            throw BadLoginException()
        }
    }

    fun authenticateToken(session: String): User {
        //check if session is in sessions table
            //if found, return user (with embedded org) of the login
            //if not, throw exception
        return User()
    }
}