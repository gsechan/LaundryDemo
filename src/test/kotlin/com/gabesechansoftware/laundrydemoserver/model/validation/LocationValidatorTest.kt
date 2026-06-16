package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.assertEmpty
import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import com.gabesechansoftware.laundrydemoserver.model.dbview.EmbeddedAddress
import com.gabesechansoftware.laundrydemoserver.model.dbview.Location
import org.junit.jupiter.api.Test
import java.util.UUID

class LocationValidatorTest {

    val validator = LocationValidator()
    val orgId: UUID = UUID.randomUUID()
    val validAddress = EmbeddedAddress("123 Main St", null, "Chicago", "IL", "US", "60601")

    @Test
    fun `all valid, no errors`() {
        val location = Location(name = "Main Branch", address = validAddress, organizationId = orgId)
        val errors = mutableListOf<String>()
        validator.validateLocation(location, errors)
        assertEmpty(errors)
    }

    @Test
    fun `name exactly 3 characters is valid`() {
        val location = Location(name = "ABC", address = validAddress, organizationId = orgId)
        val errors = mutableListOf<String>()
        validator.validateLocation(location, errors)
        assertEmpty(errors)
    }

    @Test
    fun `name too short, an error is added`() {
        val location = Location(name = "AB", address = validAddress, organizationId = orgId)
        val errors = mutableListOf<String>()
        validator.validateLocation(location, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `empty name, an error is added`() {
        val location = Location(name = "", address = validAddress, organizationId = orgId)
        val errors = mutableListOf<String>()
        validator.validateLocation(location, errors)
        assertNotEmpty(errors)
    }
}
