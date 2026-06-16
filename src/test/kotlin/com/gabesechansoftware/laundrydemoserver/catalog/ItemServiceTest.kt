package com.gabesechansoftware.laundrydemoserver.catalog

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.EmbeddedAddress
import com.gabesechansoftware.laundrydemoserver.model.dbview.Location
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemName
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemType
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.ItemRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.LocationRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
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
import java.util.Optional
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class ItemServiceTest {

    @MockK
    private lateinit var itemRepository: ItemRepository

    @MockK
    private lateinit var addressRepository: AddressRepository

    @MockK
    private lateinit var locationRepository: LocationRepository

    @InjectMockKs
    private lateinit var service: ItemService

    private val locationId = UUID.randomUUID()
    private val org = Organization("Test Org", "en-US")
    private val postcode = "60601"
    private val location = Location(
        name = "Main Branch",
        address = EmbeddedAddress("123 Main", null, "Chicago", "IL", "US", postcode),
        organizationId = org.id,
    )
    private val user = User(name = "Gabe", email = "test@example.com", phone = "3128675309", organization = org)
    private val defaultAddress = Address(street1 = "1 Test St", city = "Chicago", state = "IL",
        country = "US", postcode = postcode, isDefault = true, user = user)

    // --- getItemsForUser ---

    @Test
    fun `getItemsForUser - uses default address when addressId is null`() {
        val item = Item(locationId = location.id, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        every { addressRepository.findFirstByUserAndIsDefault(user, true) } returns defaultAddress
        every { locationRepository.findFirstByOrganizationIdAndAddressPostcode(org.id, postcode) } returns location
        every { itemRepository.findByLocationId(location.id) } returns listOf(item)

        val result = service.getItemsForUser(user, null)

        assertEquals(1, result.size)
        assertEquals(item, result[0])
    }

    @Test
    fun `getItemsForUser - uses provided addressId when given`() {
        val item = Item(locationId = location.id, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        every { addressRepository.findById(defaultAddress.id) } returns Optional.of(defaultAddress)
        every { locationRepository.findFirstByOrganizationIdAndAddressPostcode(org.id, postcode) } returns location
        every { itemRepository.findByLocationId(location.id) } returns listOf(item)

        val result = service.getItemsForUser(user, defaultAddress.id)

        assertEquals(1, result.size)
    }

    @Test
    fun `getItemsForUser - returns empty list when no default address`() {
        every { addressRepository.findFirstByUserAndIsDefault(user, true) } returns null

        val result = service.getItemsForUser(user, null)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getItemsForUser - returns empty list when provided address not found`() {
        val id = UUID.randomUUID()
        every { addressRepository.findById(id) } returns Optional.empty()

        val result = service.getItemsForUser(user, id)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getItemsForUser - returns empty list when no matching location`() {
        every { addressRepository.findFirstByUserAndIsDefault(user, true) } returns defaultAddress
        every { locationRepository.findFirstByOrganizationIdAndPostcode(org.id, postcode) } returns null

        val result = service.getItemsForUser(user, null)

        assertTrue(result.isEmpty())
    }

    // --- getItems (direct by locationId, for admin use) ---

    @Test
    fun `getItems - returns items for location`() {
        val items = listOf(Item(locationId = locationId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING))
        every { itemRepository.findByLocationId(locationId) } returns items

        val result = service.getItems(locationId)

        assertEquals(items, result)
    }

    // --- createItem ---

    @Test
    fun `createItem - saves item with price, type and names`() {
        every { itemRepository.save(any()) } returnsArgument 0
        val upload = UploadItem(
            price = "3.50",
            itemType = ItemType.DRY_CLEANING,
            names = listOf(UploadItemName("Shirt", "en-US"), UploadItemName("Camisa", "es-ES")),
        )

        val result = service.createItem(locationId, upload)

        assertEquals(locationId, result.locationId)
        assertEquals(BigDecimal("3.50"), result.price)
        assertEquals(ItemType.DRY_CLEANING, result.itemType)
        assertEquals(2, result.names.size)
        assertEquals(result.id, result.names[0].itemId)
        verify { itemRepository.save(result) }
    }

    @Test
    fun `createItem - invalid price throws and nothing is saved`() {
        val upload = UploadItem(price = "free", itemType = ItemType.DRY_CLEANING, names = emptyList())

        assertThrows<APIErrorException> { service.createItem(locationId, upload) }
        verify(exactly = 0) { itemRepository.save(any()) }
    }

    // --- updateItem ---

    @Test
    fun `updateItem - applies price and itemType and saves`() {
        val item = Item(locationId = locationId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        every { itemRepository.findByLocationIdAndId(locationId, item.id) } returns item
        every { itemRepository.save(any()) } returnsArgument 0

        val result = service.updateItem(locationId, item.id, PatchItem(price = "5.50", itemType = ItemType.WASH_AND_FOLD, names = null))

        assertEquals(BigDecimal("5.50"), result.price)
        assertEquals(ItemType.WASH_AND_FOLD, result.itemType)
        verify { itemRepository.save(item) }
    }

    @Test
    fun `updateItem - replaces names when provided`() {
        val item = Item(locationId = locationId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        item.names.add(ItemName(itemId = item.id, name = "Old", locale = "en-US"))
        every { itemRepository.findByLocationIdAndId(locationId, item.id) } returns item
        every { itemRepository.save(any()) } returnsArgument 0

        val newNames = listOf(UploadItemName("Shirt", "en-US"), UploadItemName("Camisa", "es-ES"))
        val result = service.updateItem(locationId, item.id, PatchItem(price = null, itemType = null, names = newNames))

        assertEquals(2, result.names.size)
        assertEquals("Shirt", result.names[0].name)
        assertEquals("Camisa", result.names[1].name)
        verify { itemRepository.save(item) }
    }

    @Test
    fun `updateItem - invalid price throws and nothing is saved`() {
        val item = Item(locationId = locationId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        every { itemRepository.findByLocationIdAndId(locationId, item.id) } returns item

        assertThrows<APIErrorException> {
            service.updateItem(locationId, item.id, PatchItem(price = "not-a-number", itemType = null, names = null))
        }
        verify(exactly = 0) { itemRepository.save(any()) }
    }

    @Test
    fun `updateItem - item not in location throws and nothing is saved`() {
        val id = UUID.randomUUID()
        every { itemRepository.findByLocationIdAndId(locationId, id) } returns null

        assertThrows<EntityDoesNotExistException> {
            service.updateItem(locationId, id, PatchItem(price = "5.00", itemType = null, names = null))
        }
        verify(exactly = 0) { itemRepository.save(any()) }
    }

    // --- deleteItem ---

    @Test
    fun `deleteItem - existing item is deleted`() {
        val item = Item(locationId = locationId, price = BigDecimal("1.00"), itemType = ItemType.DRY_CLEANING)
        every { itemRepository.findByLocationIdAndId(locationId, item.id) } returns item
        every { itemRepository.delete(item) } just Runs

        service.deleteItem(locationId, item.id)

        verify { itemRepository.delete(item) }
    }

    @Test
    fun `deleteItem - item not in location throws and nothing is deleted`() {
        val id = UUID.randomUUID()
        every { itemRepository.findByLocationIdAndId(locationId, id) } returns null

        assertThrows<EntityDoesNotExistException> { service.deleteItem(locationId, id) }
        verify(exactly = 0) { itemRepository.delete(any()) }
    }
}
