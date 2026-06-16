package com.gabesechansoftware.laundrydemoserver.model.customerview

import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemName
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemType
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class ItemConversionTests {

    private val locationId = UUID.randomUUID()

    @Test
    fun `Test conversion works and returns picked name`() {
        val name1 = ItemName(null, name = "1st", locale = "en-US")
        val name2 = ItemName(null, name = "2nd", locale = "en-CA")
        val name3 = ItemName(null, name = "3rd", locale = "es-MX")
        val item = Item(locationId = locationId, price = BigDecimal.ONE, names = mutableListOf(name1, name2, name3))

        val result = item.toCustomer("en-CA")
        assertEquals(item.id.toString(), result.id)
        assertEquals(item.price.toString(), result.price)
        assertEquals(name2.name, result.name)
    }

    @Test
    fun `Test unknown item returned on no locale match`() {
        val item = Item(locationId = locationId, price = BigDecimal.ONE, names = mutableListOf())

        val result = item.toCustomer("en-CA")
        assertEquals("Unknown Item", result.name)
    }

    @Test
    fun `Test itemType is converted to its string value`() {
        val item = Item(locationId = locationId, price = BigDecimal.ONE, names = mutableListOf(), itemType = ItemType.DRY_CLEANING)

        val result = item.toCustomer("en-CA")
        assertEquals("DRY_CLEANING", result.itemType)
    }

    @Test
    fun `Test wash and fold itemType is converted to its string value`() {
        val item = Item(locationId = locationId, price = BigDecimal.ONE, names = mutableListOf(), itemType = ItemType.WASH_AND_FOLD)

        val result = item.toCustomer("en-CA")
        assertEquals("WASH_AND_FOLD", result.itemType)
    }
}
