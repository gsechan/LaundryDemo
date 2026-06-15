package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.assertEmpty
import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemType
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderLine
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderState
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.String
import kotlin.test.BeforeTest
import kotlin.test.assertContains

class OrderValidatorTest {
    val validator = OrderValidator()


    var order = Order()
    var lineDc = OrderLine()
    var lineWf = OrderLine()
    var pickupAddress: Address? = Address()
    var dropoffAddress: Address? = Address()

    @BeforeTest
    fun resetOrder() {
        val now = TimeSource().now()
        pickupAddress = Address()
        dropoffAddress = Address()
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
            lines = mutableListOf(lineDc, lineWf),
        )
    }

    @Test
    fun `valid order passes as a new order`() {
        val errors = mutableListOf<String>()
        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)

        assertEmpty(errors)
    }

    @Test
    fun `valid order passes as a edited order`() {
        val errors = mutableListOf<String>()
        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, false)

        assertEmpty(errors)
    }

    @Test
    fun `no lines, an error is added`() {
        val errors = mutableListOf<String>()
        order.lines.clear()

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null user, an error is added`() {
        val errors = mutableListOf<String>()
        order.user = null

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null state, an error is added`() {
        val errors = mutableListOf<String>()
        order.state = null

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null submitted, an error is added`() {
        val errors = mutableListOf<String>()
        order.submitted = null

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null lastChange, an error is added`() {
        val errors = mutableListOf<String>()
        order.lastChange = null

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null scheduledPickup, an error is added`() {
        val errors = mutableListOf<String>()
        order.scheduledPickup = null

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null scheduledDropoff, an error is added`() {
        val errors = mutableListOf<String>()
        order.scheduledDropoff = null

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null dropoffAddress, an error is added`() {
        val errors = mutableListOf<String>()
        dropoffAddress = null

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `null pickupAddress, an error is added`() {
        val errors = mutableListOf<String>()
        pickupAddress = null

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `future lastChange date, an error is added`() {
        val errors = mutableListOf<String>()
        order.lastChange = TimeSource().now().plusYears(1)

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `lastChange before submitted, an error is added`() {
        val errors = mutableListOf<String>()
        order.lastChange = order.submitted!!.minusDays(10)

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `future submitted date, an error is added`() {
        val errors = mutableListOf<String>()
        order.submitted = TimeSource().now().plusYears(1)

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }


    @Test
    fun `future completed date, an error is added`() {
        val errors = mutableListOf<String>()
        order.completed = TimeSource().now().plusYears(1)

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `completed before submitted, an error is added`() {
        val errors = mutableListOf<String>()
        order.completed = order.submitted!!.minusDays(10)

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `completed after lastChange, an error is added`() {
        val errors = mutableListOf<String>()
        order.completed = order.lastChange!!.plusDays(10)

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `pickup before submitted, an error is added`() {
        val errors = mutableListOf<String>()
        order.scheduledPickup = order.submitted!!.minusDays(10)

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `dropoff before submitted, an error is added`() {
        val errors = mutableListOf<String>()
        order.scheduledDropoff = order.submitted!!.minusDays(10)

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `submit locale null, an error is added`() {
        val errors = mutableListOf<String>()
        lineDc.submittedLocale = null

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `org locale null, an error is added`() {
        val errors = mutableListOf<String>()
        lineDc.orgLocale = null

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `ppu null, an error is added`() {
        val errors = mutableListOf<String>()
        lineDc.pricePerUnit = null

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `dry cleaning must have quantity, or an error is added`() {
        val errors = mutableListOf<String>()
        lineDc.quantity = null

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `dry cleaning must have total, or an error is added`() {
        val errors = mutableListOf<String>()
        lineDc.totalCost = null

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `dry cleaning total must calculate, or an error is added`() {
        val errors = mutableListOf<String>()
        lineDc.totalCost = BigDecimal("100")

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `wash and fold new orders must be null quantity, or an error is added`() {
        val errors = mutableListOf<String>()
        lineWf.quantity = BigDecimal("100")

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `wash and fold new orders must be null total, or an error is added`() {
        val errors = mutableListOf<String>()
        lineWf.totalCost = BigDecimal("100")

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertNotEmpty(errors)
    }

    @Test
    fun `wash and fold old orders may be nonnull total, or an error is added`() {
        val errors = mutableListOf<String>()
        lineWf.totalCost =  BigDecimal("100")

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, false)
        assertEmpty(errors)
    }

    @Test
    fun `wash and fold old orders may be nonnull quantity, or an error is added`() {
        val errors = mutableListOf<String>()
        lineWf.quantity =  BigDecimal("100")

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, false)
        assertEmpty(errors)
    }

    @Test
    fun `completed before submitted, that specific error is added`() {
        val errors = mutableListOf<String>()
        order.completed = order.submitted!!.minusDays(1)

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertContains(errors, "The completed date must be before the submitted")
    }

    @Test
    fun `completed after lastChange, that specific error is added`() {
        val errors = mutableListOf<String>()
        order.submitted = null
        order.completed = order.lastChange!!.plusDays(1)

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertContains(errors, "The completed date must be before the submitted")
    }

    @Test
    fun `invalid pickup or dropoff address, the address validator errors are added`() {
        val addressValidator = mockk<AddressValidator>()
        every { addressValidator.validateAddress(any(), any()) } answers { (args[1] as MutableList<String>).add("Bad address") }
        val validatorWithMock = OrderValidator(addressValidator = addressValidator)
        val errors = mutableListOf<String>()

        validatorWithMock.validateOrder(order, pickupAddress, dropoffAddress, errors, true)

        assertContains(errors, "Bad address")
        verify { addressValidator.validateAddress(pickupAddress!!, errors) }
        verify { addressValidator.validateAddress(dropoffAddress!!, errors) }
    }

    @Test
    fun `unknown item type, an error is added`() {
        val errors = mutableListOf<String>()
        val lineOther = OrderLine(
            nameInSubmittedLocale = "misc",
            submittedLocale = "en-US",
            nameInOrgLocale = "misc",
            orgLocale = "es-ES",
            nameInEnglishLocale = "misc",
            pricePerUnit = BigDecimal.ONE,
            quantity = BigDecimal.ONE,
            totalCost = BigDecimal.ONE,
            itemType = ItemType.OTHER
        )
        order.lines.add(lineOther)

        validator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)
        assertContains(errors, "Unknown item type ${ItemType.OTHER}")
    }

    @Test
    fun `validateEditableFields - valid order passes`() {
        val errors = mutableListOf<String>()
        validator.validateEditableFields(order, errors)
        assertEmpty(errors)
    }

    @Test
    fun `validateEditableFields - past pickup and dropoff still pass`() {
        val errors = mutableListOf<String>()
        order.scheduledPickup = TimeSource().now().minusDays(3)
        order.scheduledDropoff = TimeSource().now().minusDays(2)
        validator.validateEditableFields(order, errors)
        assertEmpty(errors)
    }

    @Test
    fun `validateEditableFields - null state adds an error`() {
        val errors = mutableListOf<String>()
        order.state = null
        validator.validateEditableFields(order, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `validateEditableFields - null pickup adds an error`() {
        val errors = mutableListOf<String>()
        order.scheduledPickup = null
        validator.validateEditableFields(order, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `validateEditableFields - dropoff before pickup adds an error`() {
        val errors = mutableListOf<String>()
        order.scheduledDropoff = order.scheduledPickup!!.minusDays(1)
        validator.validateEditableFields(order, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `validateEditableFields - dry cleaning line missing quantity adds an error`() {
        val errors = mutableListOf<String>()
        lineDc.quantity = null
        validator.validateEditableFields(order, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `validateEditableFields - line total not product of ppu and quantity adds an error`() {
        val errors = mutableListOf<String>()
        lineDc.totalCost = BigDecimal("100")
        validator.validateEditableFields(order, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `validateEditableFields - unknown item type adds an error`() {
        val errors = mutableListOf<String>()
        lineDc.itemType = ItemType.OTHER
        validator.validateEditableFields(order, errors)
        assertNotEmpty(errors)
    }
}