package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.ItemType
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order

class OrderValidator(private val timeSource: TimeSource = TimeSource()) {
    fun validateOrder(order: Order, errors: MutableList<String>, isNewOrder: Boolean = false) {
        if (order.lines.isEmpty()) {
            errors.add("There must be at least one line in an order")
        }
        val now = timeSource.now()
        if(order.user == null) {
            errors.add("There must be a user")
        }
        if(order.state == null) {
            errors.add("There must be a state")
        }
        if(order.submitted == null) {
            errors.add("There must be a submitted date")
        }
        else if(order.submitted!!.isAfter(now)) {
            errors.add("The submission date must be in the past")
        }
        if(order.lastChange == null) {
            errors.add("There must be a submitted date")
        }
        else {
            if(order.lastChange!!.isAfter(now)) {
                errors.add("The last change date must be in the past")
            }
            else if(order.submitted != null && order.lastChange!!.isBefore(order.submitted)) {
                errors.add("The last change date must be in the past")
            }
        }
        //completed can be null, so just check other sanity checks
        if(order.completed != null) {
            if(order.completed!!.isAfter(now)) {
                errors.add("The completed date must be in the past")
            }
            if(order.submitted!= null && order.completed!!.isBefore(order.submitted)) {
                errors.add("The completed date must be before the submitted")
            }
            if(order.lastChange != null && order.completed!!.isAfter(order.lastChange)) {
                errors.add("The completed date must be before the submitted")
            }
        }

        val scheduledPickup = order.scheduledPickup
        if(scheduledPickup == null) {
            errors.add("There must be a pickup time")
        }
        else {
            if( scheduledPickup.isBefore(now)) {
                //TODO:  When we set up a real timeslot system, make sure we pick a valid timeslot
                errors.add("Pickup must be in the future")
            }
            if(order.submitted != null && scheduledPickup.isBefore(order.submitted)) {
                errors.add("The pickup can't be before the submitted date")
            }
        }
        val scheduledDropoff = order.scheduledDropoff
        if(scheduledDropoff == null) {
            errors.add("There must be a dropoff time")
        }
        else if( scheduledPickup!= null && scheduledDropoff.isBefore(scheduledPickup)) {
            //TODO:  When we set up a real timeslot system, make sure we pick a valid timeslot
            errors.add("Pickup must be in the future")
        }

        if(order.dropoffAddress == null) {
            errors.add("There must be a dropoff address")
        }

        if(order.pickupAddress == null) {
            errors.add("There must be a pickup address")
        }

        order.lines.forEach { line ->
            if(line.submittedLocale == null) {
                errors.add("There must be a submitted locale")
            }
            if(line.orgLocale == null) {
                errors.add("There must be a org locale")
            }
            if(line.pricePerUnit == null) {
                errors.add("There must be a price per unit")
            }

            when(line.itemType) {
                ItemType.WASH_AND_FOLD -> {
                    if(isNewOrder) {
                        if (line.quantity != null) {
                            errors.add("Wash and Fold lines must not have a quantity when new")
                        }
                        if (line.totalCost != null) {
                            errors.add("Wash and Fold lines must not have a total cost when new")
                        }
                    }
                }
                ItemType.DRY_CLEANING -> {
                    if (line.quantity == null) {
                        errors.add("Dry Cleaning lines must not have a quantity")
                    }
                    if (line.totalCost == null) {
                        errors.add("Dry Cleaning lines must not have a total cost")
                    }
                }
                else -> {
                    errors.add("Unknown item type ${line.itemType}")
                }
            }
            if(line.totalCost != null && line.quantity != null && line.pricePerUnit != null &&
                line.totalCost != line.quantity!!.times(line.pricePerUnit!!)) {
                errors.add("Total cost is not the product of ppu and quantity")
            }
        }
    }
}