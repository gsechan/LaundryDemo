package com.gabesechansoftware.laundrydemoserver.model.customerview

import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.DryCleanItem
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.DryCleanItemName
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class DryCleanItemConversionTests {
    //Test that the conversion is right and returns the name from locale matcher.

    @Test
    fun `Test conversion works and returns picked name`() {
        val organization = Organization()
        val name1 = DryCleanItemName(null, name = "1st", locale = "en-US")
        val name2 = DryCleanItemName(null, name = "2nd", locale = "en-CA")
        val name3 = DryCleanItemName(null, name = "3rd", locale = "es-MX")
        val dryCleanItem = DryCleanItem(organization.id, BigDecimal.ONE, mutableListOf(name1, name2, name3))

        val result = dryCleanItem.toCustomer("en-CA")
        assertEquals(dryCleanItem.id.toString(), result.id)
        assertEquals(dryCleanItem.price.toString(), result.price)
        assertEquals(name2.name, result.name)
    }

    @Test
    fun `Test unknown item returned on no locale match`() {
        val organization = Organization()
        val dryCleanItem = DryCleanItem(organization.id, BigDecimal.ONE, mutableListOf())

        val result = dryCleanItem.toCustomer("en-CA")
        assertEquals("Unknown Item", result.name)
    }

}
