package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.assertEmpty
import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import org.junit.jupiter.api.Test

class AdminValidatorTest {

    val validator = AdminValidator()

    @Test
    fun `all valid, no errors`() {
        val admin = Admin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")
        val errors = mutableListOf<String>()
        validator.validateAdmin(admin, errors)
        assertEmpty(errors)
    }

    @Test
    fun `phone is invalid, an error is added`() {
        val admin = Admin(name = "Gabe", email = "admin@provider.com", phone = "123")
        val errors = mutableListOf<String>()
        validator.validateAdmin(admin, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `phone is null, an error is added`() {
        val admin = Admin(name = "Gabe", email = "admin@provider.com", phone = null)
        val errors = mutableListOf<String>()
        validator.validateAdmin(admin, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `email is invalid, an error is added`() {
        val admin = Admin(name = "Gabe", email = "not-an-email", phone = "3128675309")
        val errors = mutableListOf<String>()
        validator.validateAdmin(admin, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `email is null, an error is added`() {
        val admin = Admin(name = "Gabe", email = null, phone = "3128675309")
        val errors = mutableListOf<String>()
        validator.validateAdmin(admin, errors)
        assertNotEmpty(errors)
    }
}
