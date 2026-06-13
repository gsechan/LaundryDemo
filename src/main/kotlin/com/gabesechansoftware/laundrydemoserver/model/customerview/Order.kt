package com.gabesechansoftware.laundrydemoserver.model.customerview

import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.getDryCleanItemNameForLocale
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.ItemType
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
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.DryCleanItem as DBDryCleanItem
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.WashFoldPrice as DBWashFoldPrice

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
            pickupAddress = pickupAddress,
            dropoffAddress = dropoffAddress,
        )

    }
}

data class UploadOrderLine(
    val itemId: String,
    val quantity: String?,
    val itemType: String,
) {
    fun toDBOrderLine(dryCleanItem: DBDryCleanItem, submittedLocale: String, orgLocale: String,
       ): DBOrderLine {

        val requestItemType = enumValueOf<ItemType>(itemType)
        val pricePerUnit = dryCleanItem.price
        val quantity =  BigDecimal(quantity!!)
        val totalCost = quantity.times(pricePerUnit!!)
        val nameInSubmitLocale = getDryCleanItemNameForLocale(dryCleanItem, submittedLocale)
        val nameInOrgsLocale = getDryCleanItemNameForLocale(dryCleanItem, orgLocale)
        val nameInDefaultLocale = getDryCleanItemNameForLocale(dryCleanItem, "en-US")

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

    fun toDBOrderLine(washFoldPrice: DBWashFoldPrice, submittedLocale: String, orgLocale: String,
    ): DBOrderLine {

        val requestItemType = enumValueOf<ItemType>(itemType)
        val pricePerUnit = washFoldPrice.price
        val quantity =  null
        val totalCost = null
        val nameInSubmitLocale = "Wash and fold"
        val nameInOrgsLocale = "Wash and fold"
        val nameInDefaultLocale = "Wash and fold"

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
    val pickupAddressId: String,
    val dropoffAddressId: String,
    val lines: List<OrderLine>
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
        id.toString(),
        state!!.toString(),
        completed?.toInstant()?.toEpochMilli(),
        lastChange!!.toInstant().toEpochMilli(),
        submitted!!.toInstant().toEpochMilli(),
        scheduledPickup!!.toInstant().toEpochMilli(),
        scheduledDropoff!!.toInstant().toEpochMilli(),
        pickupAddress!!.id.toString(),
        dropoffAddress!!.id.toString(),
        lines.map { it.toCustomer() },
    )
}

fun DBOrderLine.toCustomer(): OrderLine {
    return OrderLine(
        itemType!!.toString(),
        nameInSubmittedLocale ?: this.nameInEnglishLocale ?: "Unknown item",
        pricePerUnit!!.toString(),
        quantity?.toString(),
        totalCost?.toString()
    )
}


