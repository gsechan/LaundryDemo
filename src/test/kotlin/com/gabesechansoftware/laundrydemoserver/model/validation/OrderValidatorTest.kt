package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.assertEmpty
import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.ItemType
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderLine
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderState
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.String
import kotlin.test.BeforeTest

class OrderValidatorTest {
    val validator = OrderValidator()


    var order = Order()
    var lineDc = OrderLine()
    var lineWf = OrderLine()

    @BeforeTest
    fun resetOrder() {
        val now = TimeSource().now()
        lineDc = OrderLine(
            nameInSubmittedLocale = "pants",
            submittedLocale = "en-US",
            nameInOrgLocale = "pantalones",
            orgLocale = "es-ES",
            nameInEnglishLocale = "pants",
            pricePerUnit = BigDecimal.ONE,
            quantity = BigDecimal.ONE,
            totalCost = BigDecimal.ONE,
            itemType = ItemType.DRY_CLEANING
        )
        lineWf = OrderLine(
            nameInSubmittedLocale = "pants",
            submittedLocale = "en-US",
            nameInOrgLocale = "pantalones",
            orgLocale = "es-ES",
            nameInEnglishLocale = "pants",
            pricePerUnit = BigDecimal.ONE,
            quantity = null,
            totalCost = null,
            itemType = ItemType.WASH_AND_FOLD
        )


        order = Order(
            user = User(),
            state = OrderState.SUBMITTED,
            submitted = now.minusDays(7),
            lastChange = now.minusDays(6),
            scheduledPickup = now.plusDays(5),
            scheduledDropoff = now.plusDays(6),
            dropoffAddress = Address(),
            pickupAddress = Address(),
            lines = mutableListOf(lineDc, lineWf),
        )
    }

    @Test
    fun `valid order passes as a new order`() {
        val errors = mutableListOf<String>()
        validator.validateOrder(order, errors, true)

        assertEmpty(errors)
    }

    @Test
    fun `valid order passes as a edited order`() {
        val errors = mutableListOf<String>()
        validator.validateOrder(order, errors, false)

        assertEmpty(errors)
    }

    @Test
    fun `no lines, an error is added`() {
        val errors = mutableListOf<String>()
        order.lines.clear()

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null user, an error is added`() {
        val errors = mutableListOf<String>()
        order.user = null

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null state, an error is added`() {
        val errors = mutableListOf<String>()
        order.state = null

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null submitted, an error is added`() {
        val errors = mutableListOf<String>()
        order.submitted = null

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null lastChange, an error is added`() {
        val errors = mutableListOf<String>()
        order.lastChange = null

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null scheduledPickup, an error is added`() {
        val errors = mutableListOf<String>()
        order.scheduledPickup = null

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null scheduledDropoff, an error is added`() {
        val errors = mutableListOf<String>()
        order.scheduledDropoff = null

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null dropoffAddress, an error is added`() {
        val errors = mutableListOf<String>()
        order.dropoffAddress = null

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null pickupAddress, an error is added`() {
        val errors = mutableListOf<String>()
        order.pickupAddress = null

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `future lastChange date, an error is added`() {
        val errors = mutableListOf<String>()
        order.lastChange = TimeSource().now().plusYears(1)

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `lastChange before submitted, an error is added`() {
        val errors = mutableListOf<String>()
        order.lastChange = order.submitted!!.minusDays(10)

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `future submitted date, an error is added`() {
        val errors = mutableListOf<String>()
        order.submitted = TimeSource().now().plusYears(1)

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }


    @Test
    fun `future completed date, an error is added`() {
        val errors = mutableListOf<String>()
        order.completed = TimeSource().now().plusYears(1)

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `completed before submitted, an error is added`() {
        val errors = mutableListOf<String>()
        order.completed = order.submitted!!.minusDays(10)

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `completed after lastChange, an error is added`() {
        val errors = mutableListOf<String>()
        order.completed = order.lastChange!!.plusDays(10)

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `pickup before submitted, an error is added`() {
        val errors = mutableListOf<String>()
        order.scheduledPickup = order.submitted!!.minusDays(10)

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `dropoff before submitted, an error is added`() {
        val errors = mutableListOf<String>()
        order.scheduledDropoff = order.submitted!!.minusDays(10)

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `submit locale null, an error is added`() {
        val errors = mutableListOf<String>()
        lineDc.submittedLocale = null

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `org locale null, an error is added`() {
        val errors = mutableListOf<String>()
        lineDc.orgLocale = null

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `ppu null, an error is added`() {
        val errors = mutableListOf<String>()
        lineDc.pricePerUnit = null

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `dry cleaning must have quantity, or an error is added`() {
        val errors = mutableListOf<String>()
        lineDc.quantity = null

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `dry cleaning must have total, or an error is added`() {
        val errors = mutableListOf<String>()
        lineDc.totalCost = null

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `dry cleaning total must calculate, or an error is added`() {
        val errors = mutableListOf<String>()
        lineDc.totalCost = BigDecimal("100")

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `wash and fold new orders must be null quantity, or an error is added`() {
        val errors = mutableListOf<String>()
        lineWf.quantity = BigDecimal("100")

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `wash and fold new orders must be null total, or an error is added`() {
        val errors = mutableListOf<String>()
        lineWf.totalCost = BigDecimal("100")

        validator.validateOrder(order, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `wash and fold old orders may be nonnull total, or an error is added`() {
        val errors = mutableListOf<String>()
        lineWf.totalCost =  BigDecimal("100")

        validator.validateOrder(order, errors, false)
        assertEmpty(errors)
    }

    @Test
    fun `wash and fold old orders may be nonnull quantity, or an error is added`() {
        val errors = mutableListOf<String>()
        lineWf.quantity =  BigDecimal("100")

        validator.validateOrder(order, errors, false)
        assertEmpty(errors)
    }
}