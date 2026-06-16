package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.catalog.ItemService
import com.gabesechansoftware.laundrydemoserver.catalog.PatchItem
import com.gabesechansoftware.laundrydemoserver.catalog.UploadItem
import com.gabesechansoftware.laundrydemoserver.catalog.UploadItemName
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemType
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.Runs
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class ItemAdminControllerTest {

    @MockK
    private lateinit var itemService: ItemService

    @MockK
    private lateinit var adminAuthorizationService: AdminAuthorizationService

    @InjectMockKs
    private lateinit var controller: ItemAdminController

    private val authedAdmin = Admin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")
    private val orgId = UUID.randomUUID()

    private fun canEdit(value: Boolean) {
        every {
            adminAuthorizationService.permissionsCheckAny(
                listOf(AdminPermissions.EDIT_ORG, AdminPermissions.CREATE_ORG),
                authedAdmin
            )
        } returns value
    }

    @Test
    fun `listItems - any admin can list, returns the views`() {
        val item = Item(organization = orgId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        every { itemService.getItems(orgId) } returns listOf(item)

        val response = controller.listItems(orgId)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertSize(1, response.data)
        assertEquals("DRY_CLEANING", response.data[0].itemType)
    }

    @Test
    fun `createItem - without edit permission returns NOT_AUTHORIZED and does not create`() {
        canEdit(false)
        val upload = UploadItem("3.50", ItemType.DRY_CLEANING, listOf(UploadItemName("Shirt", "en-US")))

        val response = controller.createItem(orgId, PostItemRequest(upload), authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        assertNull(response.data)
        verify(exactly = 0) { itemService.createItem(any(), any()) }
    }

    @Test
    fun `createItem - with edit permission creates and returns the view`() {
        canEdit(true)
        val upload = UploadItem("3.50", ItemType.DRY_CLEANING, listOf(UploadItemName("Shirt", "en-US")))
        val created = Item(organization = orgId, price = BigDecimal("3.50"), itemType = ItemType.DRY_CLEANING)
        every { itemService.createItem(orgId, upload) } returns created

        val response = controller.createItem(orgId, PostItemRequest(upload), authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertEquals("3.50", response.data.price)
        assertEquals("DRY_CLEANING", response.data.itemType)
        verify { itemService.createItem(orgId, upload) }
    }

    @Test
    fun `updateItem - without edit permission returns NOT_AUTHORIZED and does not update`() {
        canEdit(false)
        val itemId = UUID.randomUUID()

        val response = controller.updateItem(orgId, itemId, PatchItemRequest(PatchItem("5.00", null, null)), authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        assertNull(response.data)
        verify(exactly = 0) { itemService.updateItem(any(), any(), any()) }
    }

    @Test
    fun `updateItem - with edit permission updates and returns the view`() {
        canEdit(true)
        val itemId = UUID.randomUUID()
        val patch = PatchItem("5.00", ItemType.WASH_AND_FOLD, null)
        val updated = Item(organization = orgId, price = BigDecimal("5.00"), itemType = ItemType.WASH_AND_FOLD)
        every { itemService.updateItem(orgId, itemId, patch) } returns updated

        val response = controller.updateItem(orgId, itemId, PatchItemRequest(patch), authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertEquals("5.00", response.data.price)
        assertEquals("WASH_AND_FOLD", response.data.itemType)
        verify { itemService.updateItem(orgId, itemId, patch) }
    }

    @Test
    fun `deleteItem - without edit permission returns NOT_AUTHORIZED and does not delete`() {
        canEdit(false)
        val itemId = UUID.randomUUID()

        val response = controller.deleteItem(orgId, itemId, authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { itemService.deleteItem(any(), any()) }
    }

    @Test
    fun `deleteItem - with edit permission deletes and returns success`() {
        canEdit(true)
        val itemId = UUID.randomUUID()
        every { itemService.deleteItem(orgId, itemId) } just Runs

        val response = controller.deleteItem(orgId, itemId, authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        verify { itemService.deleteItem(orgId, itemId) }
    }

}
