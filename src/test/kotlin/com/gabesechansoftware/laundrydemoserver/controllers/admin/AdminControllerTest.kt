package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.admins.AdminService
import com.gabesechansoftware.laundrydemoserver.admins.UploadAdmin
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
class AdminControllerTest {

    @MockK
    private lateinit var adminService: AdminService

    @MockK
    private lateinit var adminAuthorizationService: AdminAuthorizationService

    @InjectMockKs
    private lateinit var controller: AdminController

    private val authedAdmin = Admin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")
    private val upload = UploadAdmin(name = "New", email = "new@provider.com", phone = "2065551212")
    private val request = CreateAdminRequest(upload, "password123")

    @Test
    fun `createAdmin - without CREATE_ADMIN returns NOT_AUTHORIZED and does not create`() {
        every {
            adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.CREATE_ADMIN), authedAdmin)
        } returns false

        val response = controller.createAdmin(request, authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        assertNull(response.data)
        verify(exactly = 0) { adminService.createAdmin(any(), any()) }
    }

    @Test
    fun `createAdmin - with CREATE_ADMIN creates and returns the admin view`() {
        val created = Admin(name = "New", email = "new@provider.com", phone = "2065551212")
        every {
            adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.CREATE_ADMIN), authedAdmin)
        } returns true
        every { adminService.createAdmin(upload, "password123") } returns created

        val response = controller.createAdmin(request, authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertEquals("New", response.data.name)
        assertEquals("new@provider.com", response.data.email)
        assertEquals("2065551212", response.data.phone)
        verify { adminService.createAdmin(upload, "password123") }
    }
}
