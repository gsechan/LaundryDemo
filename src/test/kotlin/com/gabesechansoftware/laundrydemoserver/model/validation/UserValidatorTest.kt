package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.assertEmpty
import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadUser
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class UserValidatorTest {

    val validator = UserValidator()

    @Test
    fun `name is invalid, an error is added`() {
        val org = Organization()
        val user = User("a","test@example.com","3128675309",org,mutableListOf())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertNotEmpty(errors)
    }

    fun `name is null, an error is added`() {
        val org = Organization()
        val user = User(null,"test@example.com","3128675309",org,mutableListOf())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `phone is invalid, an error is added`() {
        val org = Organization()
        val user = User("Gabe","test@example.com","123",org,mutableListOf())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertNotEmpty(errors)

    }

    @Test
    fun `phone is null, an error is added`() {
        val org = Organization()
        val user = User("Gabe","test@example.com", null,org,mutableListOf())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertNotEmpty(errors)

    }


    @Test
    fun `email is invalid, an error is added`() {
        val org = Organization()
        val user = User("Gabe","xxx", "3128675309", org,mutableListOf())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertNotEmpty(errors)
    }


    @Test
    fun `all valid, no errors`() {
        val org = Organization()
        val user = User("Gabe","test@example.com","3128675309", org,mutableListOf())
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertEmpty(errors)
    }

    @Test
    fun `too many addresses, adds an error`() {
        val org = Organization()
        val address = Address("st",null,"city","state","country", "p")
        val user = User("Gabe","test@example.com","3128675309", org, mutableListOf(address, address, address, address, address, address))
        val errors = mutableListOf<String>()
        validator.validateUser(user, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `invalid address, adds an error`() {
        val org = Organization()
        val addressValidator = mockk<AddressValidator>()
        every { addressValidator.validateAddress(any(), any()) } answers { (args[1] as MutableList<String>).add("Error") }
        val address = Address("st",null,"city","state","country", "p")
        val user = User("Gabe","test@example.com","3128675309",org, mutableListOf(address))
        val errors = mutableListOf<String>()
        val userValidator = UserValidator(addressValidator = addressValidator)
        userValidator.validateUser(user, errors)
        assertNotEmpty(errors)
    }


}