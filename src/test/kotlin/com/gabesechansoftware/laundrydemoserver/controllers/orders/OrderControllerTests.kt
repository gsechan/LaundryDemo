package com.gabesechansoftware.laundrydemoserver.controllers.orders

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadOrder
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderState
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.orders.OrderService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class OrderControllerTests {

    @InjectMockKs
    private lateinit var orderController: OrderController

    @MockK
    private lateinit var orderService: OrderService

    @Test
    fun `allOrders returns converted orders`() {
        val user = User()
        val now = TimeSource().now()
        val order1 = Order(state = OrderState.SUBMITTED, user = user, lines = mutableListOf(), scheduledPickup = now, scheduledDropoff = now,
            submitted = now, lastChange = now, dropoffAddress = Address(), pickupAddress = Address())
        val order2 = Order(state = OrderState.SUBMITTED, user = user, lines = mutableListOf(), scheduledPickup = now, scheduledDropoff = now,
            submitted = now, lastChange = now, dropoffAddress = Address(), pickupAddress = Address())
        every { orderService.getAllOrders(any()) } returns listOf(order1, order2)
        val result = orderController.allOrders(user)
        assertEquals(NetworkErrorType.NONE.toString(), result.errorType)
        assertSize(2, result.data!!)
        assertEquals(order1.id.toString(), result.data[0].id)
        assertEquals(order2.id.toString(), result.data[1].id)
    }

    @Test
    fun `newOrder returns converted orders`() {
        val user = User()
        val now = TimeSource().now()
        val order1 = Order(state = OrderState.SUBMITTED, user = user, lines = mutableListOf(), scheduledPickup = now, scheduledDropoff = now,
            submitted = now, lastChange = now, dropoffAddress = Address(), pickupAddress = Address())
        every { orderService.postUserOrder(any(), any(), any()) } returns order1
        val request = PostOrderRequest(
            UploadOrder(
                lines = emptyList(),
                scheduledPickup = 0L,
                scheduledDropoff = 0L,
                pickupAddress = "",
                dropoffAddress = "",
            )
        )
        val result = orderController.newOrder(user, request, "en-US")
        assertEquals(NetworkErrorType.NONE.toString(), result.errorType)
        assertEquals(order1.id.toString(), result.data!!.order.id)
    }

}