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
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class ItemControllerTest {
    @MockK
    private lateinit var itemService: ItemService

    @InjectMockKs
    private lateinit var controller: ItemController

    @Test
    fun `getItems returns a converted version of the returned values`() {
        val org = Organization()
        val user = User("Gabe", "test@example.com", "3128675309", org, mutableListOf())
        val name1 = ItemName(null, name = "Pants", locale = "en-US")
        val item1 = Item(org.id, BigDecimal.ONE, mutableListOf(name1), ItemType.DRY_CLEANING)
        val name2 = ItemName(null, name = "Wash and fold", locale = "en-US")
        val item2 = Item(org.id, BigDecimal.TEN, mutableListOf(name2), ItemType.WASH_AND_FOLD)
        every { itemService.getItems(org.id) } returns listOf(item1, item2)

        val result = controller.getItems(user, "en-US")

        assertEquals(NetworkErrorType.NONE.toString(), result.errorType)
        assertEquals(listOf(item1.toCustomer("en-US"), item2.toCustomer("en-US")), result.data!!.items)
    }

    @Test
    fun `getItems returns an empty list when there are no items`() {
        val org = Organization()
        val user = User("Gabe", "test@example.com", "3128675309", org, mutableListOf())
        every { itemService.getItems(org.id) } returns emptyList()

        val result = controller.getItems(user, "en-US")

        assertEquals(NetworkErrorType.NONE.toString(), result.errorType)
        assertEquals(emptyList(), result.data!!.items)
    }
}
