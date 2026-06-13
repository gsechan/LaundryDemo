package com.gabesechansoftware.laundrydemoserver.model.customerview

import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item as CustomerItem
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemName
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemType
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class ItemConversionTests {
    //Test that the conversion is right and returns the name from locale matcher.

    @Test
    fun `Test conversion works and returns picked name`() {
        val organization = Organization()
        val name1 = ItemName(null, name = "1st", locale = "en-US")
        val name2 = ItemName(null, name = "2nd", locale = "en-CA")
        val name3 = ItemName(null, name = "3rd", locale = "es-MX")
        val dryCleanItem = CustomerItem(organization.id, BigDecimal.ONE, mutableListOf(name1, name2, name3))

        val result = dryCleanItem.toCustomer("en-CA")
        assertEquals(dryCleanItem.id.toString(), result.id)
        assertEquals(dryCleanItem.price.toString(), result.price)
        assertEquals(name2.name, result.name)
    }

    @Test
    fun `Test unknown item returned on no locale match`() {
        val organization = Organization()
        val dryCleanItem = CustomerItem(organization.id, BigDecimal.ONE, mutableListOf())

        val result = dryCleanItem.toCustomer("en-CA")
        assertEquals("Unknown Item", result.name)
    }

    @Test
    fun `Test itemType is converted to its string value`() {
        val organization = Organization()
        val dryCleanItem = CustomerItem(organization.id, BigDecimal.ONE, mutableListOf(), ItemType.DRY_CLEANING)

        val result = dryCleanItem.toCustomer("en-CA")
        assertEquals("DRY_CLEANING", result.itemType)
    }

    @Test
    fun `Test wash and fold itemType is converted to its string value`() {
        val organization = Organization()
        val dryCleanItem = CustomerItem(organization.id, BigDecimal.ONE, mutableListOf(), ItemType.WASH_AND_FOLD)

        val result = dryCleanItem.toCustomer("en-CA")
        assertEquals("WASH_AND_FOLD", result.itemType)
    }

}
