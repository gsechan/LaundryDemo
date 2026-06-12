package com.gabesechansoftware.laundrydemoserver.model.customerview

import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address as DBAddress
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User as DBUser

data class User(
    val name: String,
    val email: String?,
    val phone: String,
    val addresses: List<Address>
)

data class Address(
    val id: String,
    val street1: String,
    val street2: String?,
    val city: String,
    val state: String,
    val country: String,
    val postcode: String
)

fun DBUser.toCustomerFacing(): User {
    val sorted = this.addresses.sortedBy { if (it.isDefault!!) 0 else 1 }

    return User(name!!, email, phone!!, sorted.map{it.toCustomerFacing()})
}

fun DBAddress.toCustomerFacing(): Address {
    return Address(id.toString(), street1!!, street2, city!!, state!!, country!!, postcode!!)
}
