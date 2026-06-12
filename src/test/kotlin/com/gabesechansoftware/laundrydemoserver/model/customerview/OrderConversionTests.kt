package com.gabesechansoftware.laundrydemoserver.model.customerview

import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.ItemType
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderState
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order as DBOrder
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderLine as DBOrderLine
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

class OrderConversionTests {

    @Test
    fun `Order line converts with submitted locale name`() {
        val line = DBOrderLine(
            "pants", "en-US",
            "pantalones", "es-US",
            "default",
            BigDecimal.TWO,
            BigDecimal.TEN,
            BigDecimal.TEN.times(BigDecimal.TWO),
            ItemType.DRY_CLEANING
        )

        val result = line.toCustomer()
        assertEquals(line.nameInSubmittedLocale, result.name)
        assertOrderLineEqual(line, result)
    }

    @Test
    fun `Order line converts with english locale name`() {
        val line = DBOrderLine(
            null, "en-US",
            "pantalones", "es-US",
            "default",
            BigDecimal.TWO,
            BigDecimal.TEN,
            BigDecimal.TEN.times(BigDecimal.TWO),
            ItemType.DRY_CLEANING
        )

        val result = line.toCustomer()

        assertEquals(line.nameInEnglishLocale, result.name)
        assertOrderLineEqual(line, result)
    }

    @Test
    fun `Order line converts with unknown item name`() {
        val line = DBOrderLine(
            null, "en-US",
            "pantalones", "es-US",
            null,
            BigDecimal.TWO,
            BigDecimal.TEN,
            BigDecimal.TEN.times(BigDecimal.TWO),
            ItemType.DRY_CLEANING
        )

        val result = line.toCustomer()

        assertEquals("Unknown item", result.name)
        assertOrderLineEqual(line, result)
    }

    @Test
    fun `Order converts correctly`() {
        val timeSouce = TimeSource()
        val now = timeSouce.now()
        val line = DBOrderLine(
            null, "en-US",
            "pantalones", "es-US",
            "default",
            BigDecimal.TWO,
            BigDecimal.TEN,
            BigDecimal.TEN.times(BigDecimal.TWO),
            ItemType.DRY_CLEANING
        )
        val order = DBOrder(
            User(),
            OrderState.SUBMITTED,
            mutableListOf(line),
            now,
            now.plusDays(1),
            now.plusDays(2),
            now.plusDays(3),
            now.plusDays(4),
            Address(),
            Address(),
        )
        val result = order.toCustomer()

        assertEquals(order.id.toString(), result.id)
        assertEquals(order.state.toString(), result.state)
        assertEquals(order.completed?.toInstant()?.toEpochMilli(), result.completed)
        assertEquals(order.lastChange?.toInstant()?.toEpochMilli(), result.lastChange)
        assertEquals(order.submitted?.toInstant()?.toEpochMilli(), result.submitted)
        assertEquals(order.scheduledPickup?.toInstant()?.toEpochMilli(), result.scheduledPickup)
        assertEquals(order.scheduledDropoff?.toInstant()?.toEpochMilli(), result.scheduledDropoff)
        assertEquals(order.pickupAddress!!.id.toString(), result.pickupAddressId)
        assertEquals(order.dropoffAddress!!.id.toString(), result.dropoffAddressId)

        assertSize(1, result.lines)
        assertOrderLineEqual(line, result.lines[0])

    }

    fun assertOrderLineEqual(dbline: DBOrderLine, customerLine: OrderLine) {
        assertEquals(dbline.itemType.toString(), customerLine.itemType)
        assertEquals(dbline.pricePerUnit.toString(), customerLine.pricePerUnit)
        assertEquals(dbline.quantity.toString(), customerLine.quantity)
        assertEquals(dbline.totalCost.toString(), customerLine.totalCost)

    }

}
