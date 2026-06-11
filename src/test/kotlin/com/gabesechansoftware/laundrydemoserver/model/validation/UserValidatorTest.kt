package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.assertEmpty
import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import com.gabesechansoftware.laundrydemoserver.model.customerview.User
import org.junit.jupiter.api.Test

class UserValidatorTest {

    val validator = UserValidator()

    @Test
    fun `name is invalid, an error is added`() {
        val user = User("a","test@example.com","3128675309",emptyList())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `phone is invalid, an error is added`() {
        val user = User("Gabe","test@example.com","333",emptyList())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `email is invalid, an error is added`() {
        val user = User("Gabe","test","3128675309",emptyList())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `all valid, no errors`() {
        val user = User("Gabe","test@example.com","3128675309",emptyList())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertEmpty(errors)
    }

}