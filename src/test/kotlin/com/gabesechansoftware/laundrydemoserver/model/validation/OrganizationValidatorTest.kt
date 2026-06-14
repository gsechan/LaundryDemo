package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.assertEmpty
import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import org.junit.jupiter.api.Test

class OrganizationValidatorTest {

    val validator = OrganizationValidator()

    @Test
    fun `all valid, no errors`() {
        val org = Organization(name = "Laundry", defaultLocale = "en-US")
        val errors = mutableListOf<String>()
        validator.validateOrganization(org, errors)
        assertEmpty(errors)
    }

    @Test
    fun `name is null, an error is added`() {
        val org = Organization(name = null, defaultLocale = "en-US")
        val errors = mutableListOf<String>()
        validator.validateOrganization(org, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `name too short, an error is added`() {
        val org = Organization(name = "Lau", defaultLocale = "en-US")
        val errors = mutableListOf<String>()
        validator.validateOrganization(org, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `default locale is null, an error is added`() {
        val org = Organization(name = "Laundry", defaultLocale = null)
        val errors = mutableListOf<String>()
        validator.validateOrganization(org, errors)
        assertNotEmpty(errors)
    }
}
