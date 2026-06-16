package com.gabesechansoftware.laundrydemoserver.controllers.catalog

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.catalog.ItemService
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomer
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemName
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemType
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class ItemControllerTest {
    @MockK
    private lateinit var itemService: ItemService

    @InjectMockKs
    private lateinit var controller: ItemController

    private val org = Organization()
    private val user = User(name = "Gabe", email = "test@example.com", phone = "3128675309", organization = org)
    private val locationId = UUID.randomUUID()

    @Test
    fun `getItems returns a converted version of the returned values`() {
        val name1 = ItemName(null, name = "Pants", locale = "en-US")
        val item1 = Item(locationId = locationId, price = BigDecimal.ONE, names = mutableListOf(name1), itemType = ItemType.DRY_CLEANING)
        val name2 = ItemName(null, name = "Wash and fold", locale = "en-US")
        val item2 = Item(locationId = locationId, price = BigDecimal.TEN, names = mutableListOf(name2), itemType = ItemType.WASH_AND_FOLD)
        every { itemService.getItemsForUser(user, null) } returns listOf(item1, item2)

        val result = controller.getItems(user, "en-US", null)

        assertEquals(NetworkErrorType.NONE.toString(), result.errorType)
        assertEquals(listOf(item1.toCustomer("en-US"), item2.toCustomer("en-US")), result.data!!.items)
    }

    @Test
    fun `getItems returns an empty list when there are no items`() {
        every { itemService.getItemsForUser(user, null) } returns emptyList()

        val result = controller.getItems(user, "en-US", null)

        assertEquals(NetworkErrorType.NONE.toString(), result.errorType)
        assertEquals(emptyList(), result.data!!.items)
    }

    @Test
    fun `getItems passes addressId to service when provided`() {
        val addressId = UUID.randomUUID()
        every { itemService.getItemsForUser(user, addressId) } returns emptyList()

        val result = controller.getItems(user, "en-US", addressId)

        assertEquals(NetworkErrorType.NONE.toString(), result.errorType)
        assertEquals(emptyList(), result.data!!.items)
    }
}
