package com.gabesechansoftware.laundrydemoserver.admins

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.authentication.AdminLoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AdminServiceTest {

    @MockK
    private lateinit var adminRepository: AdminRepository

    @MockK
    private lateinit var adminLoginAuthenticator: AdminLoginAuthenticator

    @InjectMockKs
    private lateinit var service: AdminService

    private val validUpload = UploadAdmin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")
    private val validPassword = "password123"

    @Test
    fun `createAdmin - valid input saves the admin and creates the password`() {
        every { adminRepository.save(any()) } returnsArgument 0
        every { adminLoginAuthenticator.createPasswordForAdmin(any(), any()) } just Runs

        val result = service.createAdmin(validUpload, validPassword)

        assertEquals("Gabe", result.name)
        assertEquals("admin@provider.com", result.email)
        assertEquals("3128675309", result.phone)
        verify { adminRepository.save(result) }
        verify { adminLoginAuthenticator.createPasswordForAdmin(result, validPassword) }
    }

    @Test
    fun `createAdmin - invalid email throws and nothing is saved`() {
        assertThrows<APIErrorException> {
            service.createAdmin(validUpload.copy(email = "not-an-email"), validPassword)
        }
        verify(exactly = 0) { adminRepository.save(any()) }
        verify(exactly = 0) { adminLoginAuthenticator.createPasswordForAdmin(any(), any()) }
    }

    @Test
    fun `createAdmin - invalid phone throws and nothing is saved`() {
        assertThrows<APIErrorException> {
            service.createAdmin(validUpload.copy(phone = "123"), validPassword)
        }
        verify(exactly = 0) { adminRepository.save(any()) }
        verify(exactly = 0) { adminLoginAuthenticator.createPasswordForAdmin(any(), any()) }
    }

    @Test
    fun `createAdmin - password failure from the authenticator propagates`() {
        every { adminRepository.save(any()) } returnsArgument 0
        every { adminLoginAuthenticator.createPasswordForAdmin(any(), any()) } throws APIErrorException(listOf("Password too short"))

        assertThrows<APIErrorException> {
            service.createAdmin(validUpload, "short")
        }
    }
}
