package com.gabesechansoftware.laundrydemoserver.model.customerview

import com.gabesechansoftware.laundrydemoserver.model.dbview.EmbeddedAddress
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.itemNameForLocale
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderState
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.String
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address as DBAddress
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User as DBUser
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order as DBOrder
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderLine as DBOrderLine
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item as DBItem

data class UploadOrder(
    val lines: List<UploadOrderLine>,
    val scheduledPickup: Long,
    val scheduledDropoff: Long,
    val pickupAddress: String,
    val dropoffAddress: String,
){
    fun toDbOrder(user: DBUser, now: OffsetDateTime, pickupAddress: DBAddress, dropoffAddress: DBAddress): DBOrder {

        return DBOrder(
            user = user,
            state = OrderState.SUBMITTED,
            submitted = now,
            lastChange = now,
            completed = null,
            scheduledPickup = Instant.ofEpochMilli(scheduledPickup).atOffset(ZoneOffset.UTC),
            scheduledDropoff = Instant.ofEpochMilli(scheduledDropoff).atOffset(ZoneOffset.UTC),
            pickupAddress = EmbeddedAddress(
                street1 = pickupAddress.street1 ?: "",
                street2 = pickupAddress.street2,
                city = pickupAddress.city ?: "",
                state = pickupAddress.state ?: "",
                country = pickupAddress.country ?: "",
                postcode = pickupAddress.postcode ?: "",
            ),
            dropoffAddress = EmbeddedAddress(
                street1 = dropoffAddress.street1 ?: "",
                street2 = dropoffAddress.street2,
                city = dropoffAddress.city ?: "",
                state = dropoffAddress.state ?: "",
                country = dropoffAddress.country ?: "",
                postcode = dropoffAddress.postcode ?: "",
            ),
        )

    }
}

data class UploadOrderLine(
    val itemId: String,
    val quantity: String?,
) {
    fun toDBOrderLine(item: DBItem, submittedLocale: String, orgLocale: String,
       ): DBOrderLine {

        val requestItemType = item.itemType
        val pricePerUnit = item.price
        val quantity =  quantity?.let { BigDecimal(it) }
        val totalCost = quantity?.times(pricePerUnit!!)
        val nameInSubmitLocale = itemNameForLocale(item, submittedLocale)
        val nameInOrgsLocale = itemNameForLocale(item, orgLocale)
        val nameInDefaultLocale = itemNameForLocale(item, "en-US")

        return DBOrderLine(
            itemType = requestItemType,
            pricePerUnit =  pricePerUnit,
            quantity = quantity,
            totalCost = totalCost,
            nameInSubmittedLocale = nameInSubmitLocale,
            submittedLocale = submittedLocale,
            nameInOrgLocale = nameInOrgsLocale,
            orgLocale = orgLocale,
            nameInEnglishLocale = nameInDefaultLocale
        )
    }
}

data class Order(
    val id: String,
    val state: String,
    val completed: Long?,
    val lastChange: Long,
    val submitted: Long,
    val scheduledPickup: Long,
    val scheduledDropoff: Long,
    val pickupAddress: OrderAddress,
    val dropoffAddress: OrderAddress,
    val lines: List<OrderLine>
)

data class OrderAddress(
    val street1: String?,
    val street2: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val postcode: String?,
)

data class OrderLine(
    val itemType: String,
    val name: String,
    val pricePerUnit: String,
    val quantity: String?,
    val totalCost: String?,
)

fun DBOrder.toCustomer(): Order {
    return Order(
        id = id.toString(),
        state = state!!.toString(),
        completed = completed?.toInstant()?.toEpochMilli(),
        lastChange = lastChange!!.toInstant().toEpochMilli(),
        submitted = submitted!!.toInstant().toEpochMilli(),
        scheduledPickup = scheduledPickup!!.toInstant().toEpochMilli(),
        scheduledDropoff = scheduledDropoff!!.toInstant().toEpochMilli(),
        pickupAddress = OrderAddress(
            street1 = pickupAddress?.street1,
            street2 = pickupAddress?.street2,
            city = pickupAddress?.city,
            state = pickupAddress?.state,
            country = pickupAddress?.country,
            postcode = pickupAddress?.postcode,
        ),
        dropoffAddress = OrderAddress(
            street1 = dropoffAddress?.street1,
            street2 = dropoffAddress?.street2,
            city = dropoffAddress?.city,
            state = dropoffAddress?.state,
            country = dropoffAddress?.country,
            postcode = dropoffAddress?.postcode,
        ),
        lines = lines.map { it.toCustomer() },
    )
}

fun DBOrderLine.toCustomer(): OrderLine {
    return OrderLine(
        itemType = itemType!!.toString(),
        name = nameInSubmittedLocale ?: this.nameInEnglishLocale ?: "Unknown item",
        pricePerUnit = pricePerUnit!!.toString(),
        quantity = quantity?.toString(),
        totalCost = totalCost?.toString()
    )
}


