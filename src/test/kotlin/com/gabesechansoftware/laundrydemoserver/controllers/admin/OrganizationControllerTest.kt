package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.organizations.OrganizationService
import com.gabesechansoftware.laundrydemoserver.organizations.PatchOrganization
import com.gabesechansoftware.laundrydemoserver.organizations.UploadOrganization
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.Runs
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
class OrganizationControllerTest {

    @MockK
    private lateinit var organizationService: OrganizationService

    @MockK
    private lateinit var adminAuthorizationService: AdminAuthorizationService

    @InjectMockKs
    private lateinit var controller: OrganizationController

    private val authedAdmin = Admin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")

    @Test
    fun `listOrganizations - any admin can list, returns the views`() {
        val orgs = listOf(
            Organization(name = "Laundry One", defaultLocale = "en-US"),
            Organization(name = "Laundry Two", defaultLocale = "es-ES"),
        )
        every { organizationService.listAll() } returns orgs

        val response = controller.listOrganizations(authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertSize(2, response.data)
        assertEquals("Laundry One", response.data[0].name)
    }

    @Test
    fun `createOrganization - without CREATE_ORG returns NOT_AUTHORIZED and does not create`() {
        val request = CreateOrganizationRequest(UploadOrganization("Laundry", "en-US"))
        every { adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.CREATE_ORG), authedAdmin) } returns false

        val response = controller.createOrganization(request, authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        assertNull(response.data)
        verify(exactly = 0) { organizationService.createOrganization(any()) }
    }

    @Test
    fun `createOrganization - with CREATE_ORG creates and returns the view`() {
        val upload = UploadOrganization("Laundry", "en-US")
        val created = Organization(name = "Laundry", defaultLocale = "en-US")
        every { adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.CREATE_ORG), authedAdmin) } returns true
        every { organizationService.createOrganization(upload) } returns created

        val response = controller.createOrganization(CreateOrganizationRequest(upload), authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertEquals("Laundry", response.data.name)
        verify { organizationService.createOrganization(upload) }
    }

    @Test
    fun `updateOrganization - without EDIT_ORG or CREATE_ORG returns NOT_AUTHORIZED and does not update`() {
        val id = UUID.randomUUID()
        every {
            adminAuthorizationService.permissionsCheckAny(
                listOf(AdminPermissions.EDIT_ORG, AdminPermissions.CREATE_ORG),
                authedAdmin
            )
        } returns false

        val response = controller.updateOrganization(id, PatchOrganizationRequest(PatchOrganization("New Name", null, null)), authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        assertNull(response.data)
        verify(exactly = 0) { organizationService.updateOrganization(any(), any()) }
    }

    @Test
    fun `updateOrganization - with permission updates and returns the view`() {
        val id = UUID.randomUUID()
        val patch = PatchOrganization("New Name", null, null)
        val updated = Organization(name = "New Name", defaultLocale = "en-US")
        every {
            adminAuthorizationService.permissionsCheckAny(
                listOf(AdminPermissions.EDIT_ORG, AdminPermissions.CREATE_ORG),
                authedAdmin
            )
        } returns true
        every { organizationService.updateOrganization(id, patch) } returns updated

        val response = controller.updateOrganization(id, PatchOrganizationRequest(patch), authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertEquals("New Name", response.data.name)
        verify { organizationService.updateOrganization(id, patch) }
    }

    @Test
    fun `updateOrganization - changing isDeleted without DELETE_ORG returns NOT_AUTHORIZED`() {
        val id = UUID.randomUUID()
        val patch = PatchOrganization(null, null, true)
        every {
            adminAuthorizationService.permissionsCheckAny(
                listOf(AdminPermissions.EDIT_ORG, AdminPermissions.CREATE_ORG),
                authedAdmin
            )
        } returns true
        every { adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.DELETE_ORG), authedAdmin) } returns false

        val response = controller.updateOrganization(id, PatchOrganizationRequest(patch), authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        assertNull(response.data)
        verify(exactly = 0) { organizationService.updateOrganization(any(), any()) }
    }

    @Test
    fun `updateOrganization - changing isDeleted with DELETE_ORG succeeds`() {
        val id = UUID.randomUUID()
        val patch = PatchOrganization(null, null, true)
        val updated = Organization(name = "Laundry", defaultLocale = "en-US", isDeleted = true)
        every {
            adminAuthorizationService.permissionsCheckAny(
                listOf(AdminPermissions.EDIT_ORG, AdminPermissions.CREATE_ORG),
                authedAdmin
            )
        } returns true
        every { adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.DELETE_ORG), authedAdmin) } returns true
        every { organizationService.updateOrganization(id, patch) } returns updated

        val response = controller.updateOrganization(id, PatchOrganizationRequest(patch), authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertEquals(true, response.data.isDeleted)
        verify { organizationService.updateOrganization(id, patch) }
    }

    @Test
    fun `deleteOrganization - without DELETE_ORG returns NOT_AUTHORIZED and does not delete`() {
        val id = UUID.randomUUID()
        every { adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.DELETE_ORG), authedAdmin) } returns false

        val response = controller.deleteOrganization(id, authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { organizationService.deleteOrganization(any()) }
    }

    @Test
    fun `deleteOrganization - with DELETE_ORG deletes and returns success`() {
        val id = UUID.randomUUID()
        every { adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.DELETE_ORG), authedAdmin) } returns true
        every { organizationService.deleteOrganization(id) } just Runs

        val response = controller.deleteOrganization(id, authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        verify { organizationService.deleteOrganization(id) }
    }
}
