package com.gabesechansoftware.laundrydemoserver.authorization

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRole
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRoleMembership
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRoleMembershipRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRoleRepository
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

@ExtendWith(MockKExtension::class)
class AdminRoleMembershipServiceTest {

    @MockK
    private lateinit var adminRepository: AdminRepository

    @MockK
    private lateinit var adminRoleRepository: AdminRoleRepository

    @MockK
    private lateinit var membershipRepository: AdminRoleMembershipRepository

    @InjectMockKs
    private lateinit var service: AdminRoleMembershipService

    private val admin = Admin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")
    private val role = AdminRole(name = "Customer Service")

    @Test
    fun `assignRole - valid admin and role saves a membership`() {
        every { adminRepository.findById(admin.id) } returns Optional.of(admin)
        every { adminRoleRepository.findById(role.id) } returns Optional.of(role)
        every { membershipRepository.existsByAdminIdAndRoleId(admin.id, role.id) } returns false
        every { membershipRepository.save(any()) } returnsArgument 0

        val result = service.assignRole(admin.id, role.id)

        assertEquals(admin, result.admin)
        assertEquals(role, result.role)
        verify { membershipRepository.save(result) }
    }

    @Test
    fun `assignRole - missing admin throws and nothing is saved`() {
        every { adminRepository.findById(admin.id) } returns Optional.empty()

        assertThrows<EntityDoesNotExistException> {
            service.assignRole(admin.id, role.id)
        }
        verify(exactly = 0) { membershipRepository.save(any()) }
    }

    @Test
    fun `assignRole - missing role throws and nothing is saved`() {
        every { adminRepository.findById(admin.id) } returns Optional.of(admin)
        every { adminRoleRepository.findById(role.id) } returns Optional.empty()

        assertThrows<EntityDoesNotExistException> {
            service.assignRole(admin.id, role.id)
        }
        verify(exactly = 0) { membershipRepository.save(any()) }
    }

    @Test
    fun `assignRole - duplicate assignment throws and nothing is saved`() {
        every { adminRepository.findById(admin.id) } returns Optional.of(admin)
        every { adminRoleRepository.findById(role.id) } returns Optional.of(role)
        every { membershipRepository.existsByAdminIdAndRoleId(admin.id, role.id) } returns true

        assertThrows<APIErrorException> {
            service.assignRole(admin.id, role.id)
        }
        verify(exactly = 0) { membershipRepository.save(any()) }
    }

    @Test
    fun `removeMembership - existing membership is deleted`() {
        val membership = AdminRoleMembership(admin = admin, role = role)
        every { membershipRepository.findById(membership.id) } returns Optional.of(membership)
        every { membershipRepository.delete(membership) } just Runs

        service.removeMembership(membership.id)

        verify { membershipRepository.delete(membership) }
    }

    @Test
    fun `removeMembership - missing membership throws and nothing is deleted`() {
        val id = UUID.randomUUID()
        every { membershipRepository.findById(id) } returns Optional.empty()

        assertThrows<EntityDoesNotExistException> {
            service.removeMembership(id)
        }
        verify(exactly = 0) { membershipRepository.delete(any()) }
    }
}
