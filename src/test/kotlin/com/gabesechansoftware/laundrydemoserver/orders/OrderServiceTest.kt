package com.gabesechansoftware.laundrydemoserver.orders

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.catalog.ItemService
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadOrder
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadOrderLine
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemName
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemType
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderLine
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderState
import com.gabesechansoftware.laundrydemoserver.model.dbview.EmbeddedAddress
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrderRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.validation.OrderValidator
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.itemNameForLocale


@ExtendWith(MockKExtension::class)
class OrderServiceTest {

    @MockK
    lateinit var orderRepository: OrderRepository

    @InjectMockKs
    lateinit var orderService: OrderService

    @MockK
    lateinit var addressRepository: AddressRepository

    @MockK
    lateinit var itemService: ItemService

    private val now = OffsetDateTime.now(ZoneOffset.UTC)
    private val submitted = now.minusDays(2)
    private val lastChange = now.minusDays(1)
    private val completed = now.minusHours(1)
    private val scheduledPickup = now.plusDays(5)
    private val scheduledDropff = now.plusDays(6)
    private val organization = Organization("Laundry", "es-ES")
    private val pickupAddress = Address( street1 = "1")
    private val dropoffAddress = Address( street1 = "2")
    private val user = User(
        name = "Gabe",
        email = "test@example.com",
        phone = "3128675309",
        organization = organization,
        addresses = mutableListOf(pickupAddress, dropoffAddress),
    )
    private val line1 = OrderLine(
        nameInSubmittedLocale = "pants",
        submittedLocale = "en-US",
        nameInOrgLocale = "pants",
        orgLocale = "en-US",
        nameInEnglishLocale = "pants",
        pricePerUnit = BigDecimal("1.00"),
        quantity = BigDecimal("2.00"),
        totalCost = BigDecimal("2.00"),
        itemType = ItemType.DRY_CLEANING,
    )
    private val line2 = OrderLine(
        nameInSubmittedLocale = "shirts",
        submittedLocale = "en-US",
        nameInOrgLocale = "shirts",
        orgLocale = "en-US",
        nameInEnglishLocale = "shirts",
        pricePerUnit = BigDecimal("1.00"),
        quantity = BigDecimal("2.00"),
        totalCost = BigDecimal("2.00"),
        itemType = ItemType.DRY_CLEANING,
    )
    private val line3 = OrderLine(
        nameInSubmittedLocale = "dress",
        submittedLocale = "en-US",
        nameInOrgLocale = "dress",
        orgLocale = "en-US",
        nameInEnglishLocale = "dress",
        pricePerUnit = BigDecimal("1.00"),
        quantity = null,
        totalCost = null,
        itemType = ItemType.WASH_AND_FOLD,
    )


    val order1 = Order(
        user = user,
        state = OrderState.SUBMITTED,
        lines = mutableListOf(line1, line2),
        submitted = submitted,
        lastChange = lastChange,
        completed = completed,
        scheduledPickup = scheduledPickup,
        scheduledDropoff = scheduledDropff,
        pickupAddress = EmbeddedAddress(street1 = pickupAddress.street1 ?: ""),
        dropoffAddress = EmbeddedAddress(street1 = dropoffAddress.street1 ?: ""),
    )

    val order2 = Order(
        user = user,
        state = OrderState.SUBMITTED,
        lines = mutableListOf(line3),
        submitted = submitted,
        lastChange = lastChange,
        completed = completed,
        scheduledPickup = scheduledPickup,
        scheduledDropoff = scheduledDropff,
        pickupAddress = EmbeddedAddress(street1 = pickupAddress.street1 ?: ""),
        dropoffAddress = EmbeddedAddress(street1 = dropoffAddress.street1 ?: ""),
    )

    private val washFoldPrice = Item(organization.id, BigDecimal(1.0), mutableListOf(), ItemType.WASH_AND_FOLD)
    val dryCleanItemName1 = ItemName(null, "Englsh", "en-US")
    val dryCleanItemName2 = ItemName(null, "Spanish", "es-ES")
    private val dryCleanItem = Item(organization.id, BigDecimal(1.0), mutableListOf(dryCleanItemName1, dryCleanItemName2))


    @Test
    fun `getAllOrdersForCustomerView-  returns orders successfully and converted`() {
        every { orderRepository.findByUser(any()) } returns listOf(order1, order2)

        val result = orderService.getAllOrders(user)
        assertSize(2, result)
        assertEquals(order1, result[0])
        assertEquals(order2, result[1])
    }

    @Test
    fun `postUserOrder- validator fails then we throw`() {
        mockkStatic("com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemKt") {
            // test code here

            every { addressRepository.getReferenceById(pickupAddress.id) } returns pickupAddress
            every { addressRepository.getReferenceById(dropoffAddress.id) } returns dropoffAddress
            every { itemService.getItem(any(), any()) } returns dryCleanItem
            every {
                itemNameForLocale(
                    any(),
                    eq("en-US"),
                    any()
                )
            } returns dryCleanItemName1.name
            every {
                itemNameForLocale(
                    any(),
                    eq("es-ES"),
                    any()
                )
            } returns dryCleanItemName2.name
            val mockValidator = mockk<OrderValidator>()
            every {
                mockValidator.validateOrder(
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } answers { (args[3] as MutableList<String>).add("Error") }
            every { orderRepository.save(any<Order>()) } returnsArgument 0
            val service =
                OrderService(
                    orderRepository = orderRepository,
                    addressRepository = addressRepository,
                    itemService = itemService,
                    orderValidator = mockValidator,
                )

            val uploadLine1 = UploadOrderLine(
                "1d6b04c5-fcae-45af-8782-9af3f980d5b1", null
            )
            val uploadLine2 = UploadOrderLine(
                "3dbcaa3b-af68-4939-8fc1-22b44da261fb", "10.00"
            )
            val uploadOrder = UploadOrder(
                lines = listOf(uploadLine1, uploadLine2),
                scheduledPickup = scheduledPickup.toInstant().toEpochMilli(),
                scheduledDropoff = scheduledDropff.toInstant().toEpochMilli(),
                pickupAddress = pickupAddress.id.toString(),
                dropoffAddress = dropoffAddress.id.toString(),
            )

            assertThrows<APIErrorException> {
                service.postUserOrder(uploadOrder, user, "en-US")
            }
            verify(exactly = 0) { orderRepository.save(any()) }
        }
    }


    @Test
    fun `postUserOrder- valid data is saved and converted`() {
        mockkStatic("com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemKt") {

            every { addressRepository.getReferenceById(pickupAddress.id) } returns pickupAddress
            every { addressRepository.getReferenceById(dropoffAddress.id) } returns dropoffAddress
            every { itemService.getItem(any(), UUID.fromString("1d6b04c5-fcae-45af-8782-9af3f980d5b1")) } returns washFoldPrice
            every { itemService.getItem(any(), UUID.fromString("3dbcaa3b-af68-4939-8fc1-22b44da261fb")) } returns dryCleanItem
            every {
                itemNameForLocale(
                    any(),
                    eq("en-US"),
                    any()
                )
            } returns dryCleanItemName1.name
            every {
                itemNameForLocale(
                    any(),
                    eq("es-ES"),
                    any()
                )
            } returns dryCleanItemName2.name
            every { orderRepository.save(any<Order>()) } returnsArgument 0

            val uploadLine1 = UploadOrderLine(
                "1d6b04c5-fcae-45af-8782-9af3f980d5b1", null
            )
            val uploadLine2 = UploadOrderLine(
                "3dbcaa3b-af68-4939-8fc1-22b44da261fb", "10.00"
            )
            val uploadOrder = UploadOrder(
                lines = listOf(uploadLine1, uploadLine2),
                scheduledPickup = scheduledPickup.toInstant().toEpochMilli(),
                scheduledDropoff = scheduledDropff.toInstant().toEpochMilli(),
                pickupAddress = pickupAddress.id.toString(),
                dropoffAddress = dropoffAddress.id.toString(),
            )
            val timeSource = mockk<TimeSource>()
            every { timeSource.now() } returns now
            val service = OrderService(
                orderRepository = orderRepository,
                addressRepository = addressRepository,
                itemService = itemService,
                timeSource = timeSource,
            )

            val result = service.postUserOrder(uploadOrder, user, "en-US")
            verify { orderRepository.save(any()) }
            assertEquals(user, result.user)
            assertEquals(OrderState.SUBMITTED, result.state)
            assertEquals(now, result.lastChange)
            assertEquals(now, result.submitted)
            assertEquals(null, result.completed)
            assertEquals(
                scheduledPickup.toInstant().toEpochMilli(),
                result.scheduledPickup!!.toInstant().toEpochMilli()
            )
            assertEquals(
                scheduledDropff.toInstant().toEpochMilli(),
                result.scheduledDropoff!!.toInstant().toEpochMilli()
            )
            assertEquals(dropoffAddress.street1, result.dropoffAddress?.street1)
            assertEquals(pickupAddress.street1, result.pickupAddress?.street1)

            assertSize(2, result.lines)
            var line = result.lines[0]
            assertEquals(dryCleanItemName1.name, line.nameInSubmittedLocale)
            assertEquals("en-US", line.submittedLocale)
            assertEquals(dryCleanItemName2.name, line.nameInOrgLocale)
            assertEquals(organization.defaultLocale, line.orgLocale)
            assertEquals(dryCleanItemName1.name, line.nameInEnglishLocale)
            assertEquals(washFoldPrice.price, line.pricePerUnit)
            assertNull(line.quantity)
            assertNull(line.totalCost)
            assertEquals(ItemType.WASH_AND_FOLD, line.itemType)

            line = result.lines[1]
            assertEquals(dryCleanItemName1.name, line.nameInSubmittedLocale)
            assertEquals("en-US", line.submittedLocale)
            assertEquals(dryCleanItemName2.name, line.nameInOrgLocale)
            assertEquals(organization.defaultLocale, line.orgLocale)
            assertEquals(dryCleanItemName1.name, line.nameInEnglishLocale)
            assertEquals(dryCleanItem.price, line.pricePerUnit)
            assertEquals(BigDecimal(uploadLine2.quantity), line.quantity)
            assertEquals(BigDecimal(uploadLine2.quantity).times(dryCleanItem.price!!), line.totalCost)
            assertEquals(ItemType.DRY_CLEANING, line.itemType)
        }
    }
}
