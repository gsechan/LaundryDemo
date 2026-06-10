package com.gabesechansoftware.laundrydemoserver.model.customerview

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
    val price_per_unit: String,
    val quantity: String?,
    val total_cost: String?,
)
