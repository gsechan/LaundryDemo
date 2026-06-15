package com.gabesechansoftware.laundrydemoserver.catalog

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemName
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemType
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.ItemRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class ItemServiceTest {

    @MockK
    private lateinit var itemRepository: ItemRepository

    @InjectMockKs
    private lateinit var service: ItemService

    private val orgId = UUID.randomUUID()

    @Test
    fun `createItem - saves item with price, type and names`() {
        every { itemRepository.save(any()) } returnsArgument 0
        val upload = UploadItem(
            price = "3.50",
            itemType = ItemType.DRY_CLEANING,
            names = listOf(UploadItemName("Shirt", "en-US"), UploadItemName("Camisa", "es-ES")),
        )

        val result = service.createItem(orgId, upload)

        assertEquals(orgId, result.organization)
        assertEquals(BigDecimal("3.50"), result.price)
        assertEquals(ItemType.DRY_CLEANING, result.itemType)
        assertEquals(2, result.names.size)
        assertEquals(result.id, result.names[0].itemId)
        verify { itemRepository.save(result) }
    }

    @Test
    fun `createItem - invalid price throws and nothing is saved`() {
        val upload = UploadItem(price = "free", itemType = ItemType.DRY_CLEANING, names = emptyList())

        assertThrows<APIErrorException> {
            service.createItem(orgId, upload)
        }
        verify(exactly = 0) { itemRepository.save(any()) }
    }

    @Test
    fun `updateItem - applies price and itemType and saves`() {
        val item = Item(organization = orgId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        every { itemRepository.findByOrganizationAndId(orgId, item.id) } returns item
        every { itemRepository.save(any()) } returnsArgument 0

        val result = service.updateItem(orgId, item.id, PatchItem(price = "5.50", itemType = ItemType.WASH_AND_FOLD))

        assertEquals(BigDecimal("5.50"), result.price)
        assertEquals(ItemType.WASH_AND_FOLD, result.itemType)
        verify { itemRepository.save(item) }
    }

    @Test
    fun `updateItem - invalid price throws and nothing is saved`() {
        val item = Item(organization = orgId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        every { itemRepository.findByOrganizationAndId(orgId, item.id) } returns item

        assertThrows<APIErrorException> {
            service.updateItem(orgId, item.id, PatchItem(price = "not-a-number", itemType = null))
        }
        verify(exactly = 0) { itemRepository.save(any()) }
    }

    @Test
    fun `updateItem - item not in org throws and nothing is saved`() {
        val id = UUID.randomUUID()
        every { itemRepository.findByOrganizationAndId(orgId, id) } returns null

        assertThrows<EntityDoesNotExistException> {
            service.updateItem(orgId, id, PatchItem(price = "5.00", itemType = null))
        }
        verify(exactly = 0) { itemRepository.save(any()) }
    }

    @Test
    fun `deleteItem - existing item is deleted`() {
        val item = Item(organization = orgId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        every { itemRepository.findByOrganizationAndId(orgId, item.id) } returns item
        every { itemRepository.delete(item) } just Runs

        service.deleteItem(orgId, item.id)

        verify { itemRepository.delete(item) }
    }

    @Test
    fun `deleteItem - item not in org throws and nothing is deleted`() {
        val id = UUID.randomUUID()
        every { itemRepository.findByOrganizationAndId(orgId, id) } returns null

        assertThrows<EntityDoesNotExistException> {
            service.deleteItem(orgId, id)
        }
        verify(exactly = 0) { itemRepository.delete(any()) }
    }

    @Test
    fun `addItemName - adds the name to the item and saves`() {
        val item = Item(organization = orgId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        every { itemRepository.findByOrganizationAndId(orgId, item.id) } returns item
        every { itemRepository.save(any()) } returnsArgument 0

        val result = service.addItemName(orgId, item.id, "Shirt", "en-US")

        assertEquals("Shirt", result.name)
        assertEquals("en-US", result.locale)
        assertEquals(item.id, result.itemId)
        assertTrue(item.names.contains(result))
        verify { itemRepository.save(item) }
    }

    @Test
    fun `addItemName - item not in org throws`() {
        val id = UUID.randomUUID()
        every { itemRepository.findByOrganizationAndId(orgId, id) } returns null

        assertThrows<EntityDoesNotExistException> {
            service.addItemName(orgId, id, "Shirt", "en-US")
        }
        verify(exactly = 0) { itemRepository.save(any()) }
    }

    @Test
    fun `updateItemName - applies non-null fields and saves`() {
        val item = Item(organization = orgId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        val name = ItemName(itemId = item.id, name = "Old", locale = "en-US")
        item.names.add(name)
        every { itemRepository.findByOrganizationAndId(orgId, item.id) } returns item
        every { itemRepository.save(any()) } returnsArgument 0

        val result = service.updateItemName(orgId, item.id, name.id, PatchItemName(name = "New", locale = null))

        assertEquals("New", result.name)
        assertEquals("en-US", result.locale)
        verify { itemRepository.save(item) }
    }

    @Test
    fun `updateItemName - missing name throws and nothing is saved`() {
        val item = Item(organization = orgId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        every { itemRepository.findByOrganizationAndId(orgId, item.id) } returns item

        assertThrows<EntityDoesNotExistException> {
            service.updateItemName(orgId, item.id, UUID.randomUUID(), PatchItemName("New", null))
        }
        verify(exactly = 0) { itemRepository.save(any()) }
    }

    @Test
    fun `deleteItemName - removes the name and saves`() {
        val item = Item(organization = orgId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        val name = ItemName(itemId = item.id, name = "Old", locale = "en-US")
        item.names.add(name)
        every { itemRepository.findByOrganizationAndId(orgId, item.id) } returns item
        every { itemRepository.save(any()) } returnsArgument 0

        service.deleteItemName(orgId, item.id, name.id)

        assertFalse(item.names.contains(name))
        verify { itemRepository.save(item) }
    }

    @Test
    fun `deleteItemName - missing name throws and nothing is saved`() {
        val item = Item(organization = orgId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        every { itemRepository.findByOrganizationAndId(orgId, item.id) } returns item

        assertThrows<EntityDoesNotExistException> {
            service.deleteItemName(orgId, item.id, UUID.randomUUID())
        }
        verify(exactly = 0) { itemRepository.save(any()) }
    }
}
