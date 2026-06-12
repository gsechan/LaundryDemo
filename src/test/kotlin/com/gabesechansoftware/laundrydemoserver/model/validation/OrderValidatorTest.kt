package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.assertEmpty
import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadOrder
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadOrderLine
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import kotlin.String

class OrderValidatorTest {
    val validator = OrderValidator()

    val validLine= UploadOrderLine(
        "1",
        "1.00",
        "DRY_CLEANING",

        )
    val validLine2= UploadOrderLine(
        "1",
        null,
        "WASH_AND_FOLD",

        )

    @Test
    fun `no lines, an error is added`() {
        val pickup = OffsetDateTime.now()!!.plusDays(1)
        val dropoff = OffsetDateTime.now()!!.plusDays(1)
        val uploadOrder = UploadOrder(emptyList(), pickup.toInstant().toEpochMilli(), dropoff.toInstant().toEpochMilli(), "1", "2")
        val errors = mutableListOf<String>()

        validator.validateUploadOrder(uploadOrder, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `pickup in the past, an error is added`() {
        val pickup = OffsetDateTime.now()!!.minusDays(1)
        val dropoff = OffsetDateTime.now()!!.plusDays(1)
        val uploadOrder = UploadOrder(listOf(validLine), pickup.toInstant().toEpochMilli(), dropoff.toInstant().toEpochMilli(), "1", "2")
        val errors = mutableListOf<String>()

        validator.validateUploadOrder(uploadOrder, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `dropoff before pickup, an error is added`() {
        val pickup = OffsetDateTime.now()!!.plusDays(1)
        val dropoff = pickup.minusDays(1)
        val uploadOrder = UploadOrder(listOf(validLine), pickup.toInstant().toEpochMilli(), dropoff.toInstant().toEpochMilli(), "1", "2")
        val errors = mutableListOf<String>()

        validator.validateUploadOrder(uploadOrder, errors)
        assertNotEmpty(errors)
    }


    @Test
    fun `valid order, no errors added`() {
        val pickup = OffsetDateTime.now()!!.plusDays(1)
        val dropoff = pickup.plusDays(1)
        val uploadOrder = UploadOrder(listOf(validLine, validLine2), pickup.toInstant().toEpochMilli(), dropoff.toInstant().toEpochMilli(), "1", "2")
        val errors = mutableListOf<String>()

        validator.validateUploadOrder(uploadOrder, errors)
        assertEmpty(errors)
    }

    @Test
    fun `unknown line type, error added`() {
        val pickup = OffsetDateTime.now()!!.plusDays(1)
        val dropoff = pickup.plusDays(1)
        val line = UploadOrderLine("1", "1", "unknown")
        val uploadOrder = UploadOrder(listOf(line), pickup.toInstant().toEpochMilli(), dropoff.toInstant().toEpochMilli(), "1", "2")
        val errors = mutableListOf<String>()

        validator.validateUploadOrder(uploadOrder, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `dry cleaning without quantity, error added`() {
        val pickup = OffsetDateTime.now()!!.plusDays(1)
        val dropoff = pickup.plusDays(1)
        val line = UploadOrderLine("1", null, "DRY_CLEANING")
        val uploadOrder = UploadOrder(listOf(line), pickup.toInstant().toEpochMilli(), dropoff.toInstant().toEpochMilli(), "1", "2")
        val errors = mutableListOf<String>()

        validator.validateUploadOrder(uploadOrder, errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `wash and fold with quantity, error added`() {
        val pickup = OffsetDateTime.now()!!.plusDays(1)
        val dropoff = pickup.plusDays(1)
        val line = UploadOrderLine("1", "1.00", "WASH_AND_FOLD")
        val uploadOrder = UploadOrder(listOf(line), pickup.toInstant().toEpochMilli(), dropoff.toInstant().toEpochMilli(), "1", "2")
        val errors = mutableListOf<String>()

        validator.validateUploadOrder(uploadOrder, errors)
        assertNotEmpty(errors)
    }


}