package com.gabesechansoftware.laundrydemoserver.model.customerview

import kotlin.String
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order as DBOrder
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderLine as DBOrderLine

data class UploadOrder(
    val lines: List<UploadOrderLine>,
    val scheduledPickup: Long,
    val scheduledDropoff: Long,
    val pickupAddress: String,
    val dropoffAddress: String,
)

data class UploadOrderLine(
    val itemId: String,
    val quantity: String?,
    val itemType: String,
)

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
    val id: String,
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
        lastChange?.toInstant()?.toEpochMilli()!!,
        submitted?.toInstant()?.toEpochMilli()!!,
        scheduledPickup?.toInstant()?.toEpochMilli()!!,
        scheduledDropoff?.toInstant()?.toEpochMilli()!!,
        pickupAddress?.id?.toString()!!,
        dropoffAddress?.id?.toString()!!,
        lines.map { it.toCustomer() },
    )
}

fun DBOrderLine.toCustomer(): OrderLine {
    return OrderLine(
        this.id.toString(),
        this.itemType!!.toString(),
        this.nameInEnglishLocale ?: this.nameInEnglishLocale ?: "Unknown Item",
        this.pricePerUnit!!.toString(),
        this.quantity?.toString(),
        this.totalCost?.toString()
    )
}


