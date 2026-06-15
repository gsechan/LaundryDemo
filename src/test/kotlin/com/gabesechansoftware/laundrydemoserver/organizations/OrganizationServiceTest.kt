package com.gabesechansoftware.laundrydemoserver.organizations

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrganizationRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.SessionRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class OrganizationServiceTest {

    @MockK
    private lateinit var organizationRepository: OrganizationRepository

    @MockK
    private lateinit var sessionRepository: SessionRepository

    @InjectMockKs
    private lateinit var service: OrganizationService

    @Test
    fun `listAll - returns all organizations`() {
        val orgs = listOf(
            Organization(name = "Laundry One", defaultLocale = "en-US"),
            Organization(name = "Laundry Two", defaultLocale = "es-ES"),
        )
        every { organizationRepository.findAll() } returns orgs

        val result = service.listAll()

        assertSize(2, result)
        assertEquals(orgs, result)
    }

    @Test
    fun `createOrganization - valid input is saved`() {
        every { organizationRepository.save(any()) } returnsArgument 0

        val result = service.createOrganization(UploadOrganization(name = "Laundry", defaultLocale = "en-US"))

        assertEquals("Laundry", result.name)
        assertEquals("en-US", result.defaultLocale)
        verify { organizationRepository.save(result) }
    }

    @Test
    fun `createOrganization - invalid input throws and nothing is saved`() {
        assertThrows<APIErrorException> {
            service.createOrganization(UploadOrganization(name = "Lau", defaultLocale = "en-US"))
        }
        verify(exactly = 0) { organizationRepository.save(any()) }
    }

    @Test
    fun `updateOrganization - applies non-null patch fields and saves`() {
        val org = Organization(name = "Laundry", defaultLocale = "en-US")
        every { organizationRepository.findById(org.id) } returns Optional.of(org)
        every { organizationRepository.save(any()) } returnsArgument 0

        val result = service.updateOrganization(org.id, PatchOrganization(name = "Laundry Renamed", defaultLocale = null, isDeleted = null))

        assertEquals("Laundry Renamed", result.name)
        assertEquals("en-US", result.defaultLocale)
        verify { organizationRepository.save(org) }
        verify(exactly = 0) { sessionRepository.deleteByOrganizationId(any()) }
    }

    @Test
    fun `updateOrganization - setting isDeleted true soft-deletes and purges sessions`() {
        val org = Organization(name = "Laundry", defaultLocale = "en-US")
        every { organizationRepository.findById(org.id) } returns Optional.of(org)
        every { organizationRepository.save(any()) } returnsArgument 0
        every { sessionRepository.deleteByOrganizationId(org.id) } just Runs

        val result = service.updateOrganization(org.id, PatchOrganization(name = null, defaultLocale = null, isDeleted = true))

        assertTrue(result.isDeleted)
        verify { sessionRepository.deleteByOrganizationId(org.id) }
    }

    @Test
    fun `updateOrganization - setting isDeleted false does not purge sessions`() {
        val org = Organization(name = "Laundry", defaultLocale = "en-US", isDeleted = true)
        every { organizationRepository.findById(org.id) } returns Optional.of(org)
        every { organizationRepository.save(any()) } returnsArgument 0

        val result = service.updateOrganization(org.id, PatchOrganization(name = null, defaultLocale = null, isDeleted = false))

        assertEquals(false, result.isDeleted)
        verify(exactly = 0) { sessionRepository.deleteByOrganizationId(any()) }
    }

    @Test
    fun `updateOrganization - missing org throws and nothing is saved`() {
        val id = UUID.randomUUID()
        every { organizationRepository.findById(id) } returns Optional.empty()

        assertThrows<EntityDoesNotExistException> {
            service.updateOrganization(id, PatchOrganization(name = "Whatever", defaultLocale = null, isDeleted = null))
        }
        verify(exactly = 0) { organizationRepository.save(any()) }
    }

    @Test
    fun `updateOrganization - patch that makes org invalid throws and nothing is saved`() {
        val org = Organization(name = "Laundry", defaultLocale = "en-US")
        every { organizationRepository.findById(org.id) } returns Optional.of(org)

        assertThrows<APIErrorException> {
            service.updateOrganization(org.id, PatchOrganization(name = "Lau", defaultLocale = null, isDeleted = null))
        }
        verify(exactly = 0) { organizationRepository.save(any()) }
    }

    @Test
    fun `deleteOrganization - soft-deletes the org and purges sessions`() {
        val org = Organization(name = "Laundry", defaultLocale = "en-US")
        every { organizationRepository.findById(org.id) } returns Optional.of(org)
        every { organizationRepository.save(any()) } returnsArgument 0
        every { sessionRepository.deleteByOrganizationId(org.id) } just Runs

        service.deleteOrganization(org.id)

        assertTrue(org.isDeleted)
        verify { organizationRepository.save(org) }
        verify { sessionRepository.deleteByOrganizationId(org.id) }
    }

    @Test
    fun `deleteOrganization - missing org throws and nothing is changed`() {
        val id = UUID.randomUUID()
        every { organizationRepository.findById(id) } returns Optional.empty()

        assertThrows<EntityDoesNotExistException> {
            service.deleteOrganization(id)
        }
        verify(exactly = 0) { organizationRepository.save(any()) }
        verify(exactly = 0) { sessionRepository.deleteByOrganizationId(any()) }
    }
}
