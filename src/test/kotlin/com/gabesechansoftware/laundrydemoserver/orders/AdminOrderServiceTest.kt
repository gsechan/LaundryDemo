package com.gabesechansoftware.laundrydemoserver.orders

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.catalog.ItemService
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemType
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderLine
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderState
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrderRepository
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
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Optional
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AdminOrderServiceTest {

    @MockK
    private lateinit var orderRepository: OrderRepository

    @MockK
    private lateinit var addressRepository: AddressRepository

    @MockK
    private lateinit var itemService: ItemService

    @InjectMockKs
    private lateinit var service: OrderService

    @Test
    fun `listAllOrders - returns all orders`() {
        val orders = listOf(Order(state = OrderState.SUBMITTED), Order(state = OrderState.COMPLETED))
        every { orderRepository.findAll() } returns orders

        val result = service.listAllOrders()

        assertSize(2, result)
        assertEquals(orders, result)
    }

    private val futureMs = System.currentTimeMillis() + 86_400_000L
    private val future = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1)

    @Test
    fun `updateOrder - sets state, scheduled time and pickup address`() {
        val order = Order(
            state = OrderState.SUBMITTED,
            scheduledPickup = future,
            scheduledDropoff = future.plusHours(2),
        )
        every { orderRepository.findById(order.id) } returns Optional.of(order)
        every { orderRepository.save(any()) } returnsArgument 0

        val patch = PatchOrder(
            state = OrderState.COMPLETED,
            scheduledPickup = futureMs,
            scheduledDropoff = futureMs + 3_600_000L,
            pickupAddress = PatchOrderAddress("123 Main", null, "Chicago", null, null, null),
            dropoffAddress = null,
            lines = null,
        )

        val result = service.updateOrder(order.id, patch)

        assertEquals(OrderState.COMPLETED, result.state)
        assertEquals(futureMs, result.scheduledPickup!!.toInstant().toEpochMilli())
        assertEquals("123 Main", result.pickupAddress?.street1)
        assertEquals("Chicago", result.pickupAddress?.city)
        verify { orderRepository.save(order) }
    }

    @Test
    fun `updateOrder - line quantity sets quantity and derives totalCost`() {
        val line = OrderLine(pricePerUnit = BigDecimal("2.00"), itemType = ItemType.DRY_CLEANING)
        val order = Order(
            state = OrderState.SUBMITTED,
            lines = mutableListOf(line),
            scheduledPickup = future,
            scheduledDropoff = future.plusHours(2),
        )
        every { orderRepository.findById(order.id) } returns Optional.of(order)
        every { orderRepository.save(any()) } returnsArgument 0

        val patch = PatchOrder(null, null, null, null, null, listOf(PatchOrderLine(line.id, "3")))

        service.updateOrder(order.id, patch)

        assertEquals(BigDecimal("3"), line.quantity)
        assertEquals(BigDecimal("2.00").times(BigDecimal("3")), line.totalCost)
    }

    @Test
    fun `updateOrder - invalid editable fields (no schedule) throw and nothing is saved`() {
        val order = Order(state = OrderState.SUBMITTED)
        every { orderRepository.findById(order.id) } returns Optional.of(order)

        assertThrows<APIErrorException> {
            service.updateOrder(order.id, PatchOrder(OrderState.COMPLETED, null, null, null, null, null))
        }
        verify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun `updateOrder - invalid quantity throws and nothing is saved`() {
        val line = OrderLine(pricePerUnit = BigDecimal("2.00"))
        val order = Order(state = OrderState.SUBMITTED, lines = mutableListOf(line))
        every { orderRepository.findById(order.id) } returns Optional.of(order)

        assertThrows<APIErrorException> {
            service.updateOrder(order.id, PatchOrder(null, null, null, null, null, listOf(PatchOrderLine(line.id, "lots"))))
        }
        verify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun `updateOrder - unknown line throws and nothing is saved`() {
        val order = Order(state = OrderState.SUBMITTED, lines = mutableListOf())
        every { orderRepository.findById(order.id) } returns Optional.of(order)

        assertThrows<EntityDoesNotExistException> {
            service.updateOrder(order.id, PatchOrder(null, null, null, null, null, listOf(PatchOrderLine(UUID.randomUUID(), "3"))))
        }
        verify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun `updateOrder - missing order throws`() {
        val id = UUID.randomUUID()
        every { orderRepository.findById(id) } returns Optional.empty()

        assertThrows<EntityDoesNotExistException> {
            service.updateOrder(id, PatchOrder(OrderState.COMPLETED, null, null, null, null, null))
        }
    }

    @Test
    fun `deleteOrder - existing order is deleted`() {
        val order = Order(state = OrderState.SUBMITTED)
        every { orderRepository.findById(order.id) } returns Optional.of(order)
        every { orderRepository.delete(order) } just Runs

        service.deleteOrder(order.id)

        verify { orderRepository.delete(order) }
    }

    @Test
    fun `deleteOrder - missing order throws and nothing is deleted`() {
        val id = UUID.randomUUID()
        every { orderRepository.findById(id) } returns Optional.empty()

        assertThrows<EntityDoesNotExistException> {
            service.deleteOrder(id)
        }
        verify(exactly = 0) { orderRepository.delete(any()) }
    }
}
