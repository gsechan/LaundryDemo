package com.gabesechansoftware.laundrydemoserver.locations

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.EmbeddedAddress
import com.gabesechansoftware.laundrydemoserver.model.dbview.Location
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.LocationRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class LocationServiceTest {

    @MockK
    private lateinit var locationRepository: LocationRepository

    @InjectMockKs
    private lateinit var service: LocationService

    private val orgId = UUID.randomUUID()
    private val validAddress = EmbeddedAddress("123 Main St", null, "Chicago", "IL", "US", "60601")

    @Test
    fun `listByOrg - returns locations for org`() {
        val locations = listOf(
            Location(name = "Branch One", address = validAddress, organizationId = orgId),
            Location(name = "Branch Two", address = validAddress, organizationId = orgId),
        )
        every { locationRepository.findByOrganizationId(orgId) } returns locations

        val result = service.listByOrg(orgId)

        assertEquals(2, result.size)
        assertEquals(locations, result)
    }

    @Test
    fun `getLocation - returns location when found`() {
        val location = Location(name = "Main Branch", address = validAddress, organizationId = orgId)
        every { locationRepository.findById(location.id) } returns Optional.of(location)

        val result = service.getLocation(location.id)

        assertEquals(location, result)
    }

    @Test
    fun `getLocation - throws when not found`() {
        val id = UUID.randomUUID()
        every { locationRepository.findById(id) } returns Optional.empty()

        assertThrows<EntityDoesNotExistException> { service.getLocation(id) }
    }

    @Test
    fun `createLocation - saves and returns location`() {
        val upload = UploadLocation("Main Branch", "123 Main St", null, "Chicago", "IL", "US", "60601")
        every { locationRepository.save(any()) } returnsArgument 0

        val result = service.createLocation(orgId, upload)

        assertEquals("Main Branch", result.name)
        assertEquals("123 Main St", result.address.street1)
        assertEquals(orgId, result.organizationId)
        verify { locationRepository.save(any()) }
    }

    @Test
    fun `createLocation - throws when name too short`() {
        val upload = UploadLocation("AB", "123 Main St", null, "Chicago", "IL", "US", "60601")

        assertThrows<APIErrorException> { service.createLocation(orgId, upload) }
    }

    @Test
    fun `updateLocation - patches fields and saves`() {
        val location = Location(name = "Old Name", address = validAddress, organizationId = orgId)
        every { locationRepository.findById(location.id) } returns Optional.of(location)
        every { locationRepository.save(any()) } returnsArgument 0

        val patch = PatchLocation(name = "New Name", street1 = "456 Oak Ave", street2 = null,
            city = null, state = null, country = null, postcode = null)
        val result = service.updateLocation(location.id, patch)

        assertEquals("New Name", result.name)
        assertEquals("456 Oak Ave", result.address.street1)
        assertEquals("Chicago", result.address.city)
    }

    @Test
    fun `updateLocation - throws when not found`() {
        val id = UUID.randomUUID()
        every { locationRepository.findById(id) } returns Optional.empty()

        assertThrows<EntityDoesNotExistException> {
            service.updateLocation(id, PatchLocation(name = "X", null, null, null, null, null, null))
        }
    }

    @Test
    fun `updateLocation - throws when patch results in invalid name`() {
        val location = Location(name = "Main Branch", address = validAddress, organizationId = orgId)
        every { locationRepository.findById(location.id) } returns Optional.of(location)

        assertThrows<APIErrorException> {
            service.updateLocation(location.id, PatchLocation(name = "AB", null, null, null, null, null, null))
        }
    }

    @Test
    fun `deleteLocation - deletes when found`() {
        val location = Location(name = "Main Branch", address = validAddress, organizationId = orgId)
        every { locationRepository.findById(location.id) } returns Optional.of(location)
        every { locationRepository.delete(location) } returns Unit

        service.deleteLocation(location.id)

        verify { locationRepository.delete(location) }
    }

    @Test
    fun `deleteLocation - throws when not found`() {
        val id = UUID.randomUUID()
        every { locationRepository.findById(id) } returns Optional.empty()

        assertThrows<EntityDoesNotExistException> { service.deleteLocation(id) }
    }
}
