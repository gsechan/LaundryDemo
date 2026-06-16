package com.gabesechansoftware.laundrydemoserver.model.adminview

import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order

data class OrderAddressView(
    val street1: String?,
    val street2: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val postcode: String?,
)

data class AdminOrderLineView(
    val id: String,
    val name: String?,
    val itemType: String?,
    val pricePerUnit: String?,
    val quantity: String?,
    val totalCost: String?,
)

data class AdminOrderView(
    val id: String,
    val userId: String?,
    val state: String?,
    val submitted: Long?,
    val lastChange: Long?,
    val completed: Long?,
    val scheduledPickup: Long?,
    val scheduledDropoff: Long?,
    val pickupAddress: OrderAddressView,
    val dropoffAddress: OrderAddressView,
    val lines: List<AdminOrderLineView>,
)

fun Order.toAdminView() = AdminOrderView(
    id = id.toString(),
    userId = user?.id?.toString(),
    state = state?.name,
    submitted = submitted?.toInstant()?.toEpochMilli(),
    lastChange = lastChange?.toInstant()?.toEpochMilli(),
    completed = completed?.toInstant()?.toEpochMilli(),
    scheduledPickup = scheduledPickup?.toInstant()?.toEpochMilli(),
    scheduledDropoff = scheduledDropoff?.toInstant()?.toEpochMilli(),
    pickupAddress = OrderAddressView(pickupAddress?.street1, pickupAddress?.street2, pickupAddress?.city, pickupAddress?.state, pickupAddress?.country, pickupAddress?.postcode),
    dropoffAddress = OrderAddressView(dropoffAddress?.street1, dropoffAddress?.street2, dropoffAddress?.city, dropoffAddress?.state, dropoffAddress?.country, dropoffAddress?.postcode),
    lines = lines.map {
        AdminOrderLineView(
            id = it.id.toString(),
            name = it.nameInEnglishLocale ?: it.nameInSubmittedLocale,
            itemType = it.itemType?.name,
            pricePerUnit = it.pricePerUnit?.toString(),
            quantity = it.quantity?.toString(),
            totalCost = it.totalCost?.toString(),
        )
    },
)