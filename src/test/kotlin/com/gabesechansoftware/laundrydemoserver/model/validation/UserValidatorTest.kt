package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.assertEmpty
import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadUser
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class UserValidatorTest {

    val validator = UserValidator()

    @Test
    fun `name is invalid, an error is added`() {
        val user = UploadUser("a","test@example.com","3128675309",emptyList())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `phone is invalid, an error is added`() {
        val user = UploadUser("Gabe","test@example.com","333",emptyList())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `email is invalid, an error is added`() {
        val user = UploadUser("Gabe","test","3128675309",emptyList())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `all valid, no errors`() {
        val user = UploadUser("Gabe","test@example.com","3128675309",emptyList())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertEmpty(errors)
    }

    @Test
    fun `too many addresses, adds an error`() {
        val address = UploadAddress("st",null,"city","state","country", "p")
        val user = UploadUser("Gabe","test@example.com","3128675309",listOf(address, address, address, address, address, address))
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `invalid address, adds an error`() {
        val addressValidator = mockk<AddressValidator>()
        every { addressValidator.validateCustomerAddress(any(), any()) } answers { (args[1] as MutableList<String>).add("Error") }
        val address = UploadAddress("st",null,"city","state","country", "p")
        val user = UploadUser("Gabe","test@example.com","3128675309",listOf(address))
        val errors = mutableListOf<String>()
        val userValidator = UserValidator(addressValidator = addressValidator)
        userValidator.validateUser(user, errors)
        assertNotEmpty(errors)
    }


}