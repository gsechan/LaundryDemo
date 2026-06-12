package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadOrder
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.ItemType
import java.time.OffsetDateTime
import java.time.ZoneOffset

class OrderValidator {
    fun validateUploadOrder(order: UploadOrder, errors: MutableList<String>) {
        if (order.lines.isEmpty()) {
            errors.add("There must be at least one line in an order")
        }
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        if(order.scheduledPickup < now.toInstant().toEpochMilli()) {
            //TODO:  When we set up a real timeslot system, make sure we pick a valid timeslot
            errors.add("Pickup must be in the future")
        }
        if(order.scheduledDropoff <= now.toInstant().toEpochMilli()) {
            //TODO:  in addition to making sure we use a real timeslot, ensure we honor the minimum time betweek
            errors.add("Dropoff must be after pickup")
        }
        order.lines.forEach { line ->
            when(line.itemType) {
                ItemType.WASH_AND_FOLD.toString() -> {
                    if (line.quantity != null) {
                        errors.add("Wash and Fold lines must not have a quantity")
                    }
                }
                ItemType.DRY_CLEANING.toString() -> {
                    if (line.quantity == null) {
                        errors.add("Dry Cleaning lines must not have a quantity")
                    }
                }
                else -> {
                    errors.add("Unknown item type ${line.itemType}")
                }
            }
        }
    }
}