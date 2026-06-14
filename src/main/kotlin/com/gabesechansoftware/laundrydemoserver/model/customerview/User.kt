package com.gabesechansoftware.laundrydemoserver.model.customerview

import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
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

data class UploadUser(
    val name: String,
    val email: String?,
    val phone: String,
    val addresses: List<UploadAddress>
){
    fun toDBUser(org: Organization): DBUser {
        // Use null as user, db will assign user when it uploads via hibernate save
        val convertedAddresses = addresses.mapIndexed { index, address -> address.toDBAddress(null, index == 0) }.toMutableList()
        return DBUser(name = name, email = email, phone = phone, organization = org, addresses = convertedAddresses)
    }
}

data class UploadAddress(
    val street1: String,
    val street2: String?,
    val city: String,
    val state: String,
    val country: String,
    val postcode: String
) {
    fun toDBAddress(user: DBUser?, isDefault: Boolean): DBAddress {
        return DBAddress(
            street1 = street1,
            street2 = street2,
            city = city,
            state = state,
            country = country,
            postcode = postcode,
            isDefault = isDefault,
            user = user,
        )
    }
}

data class PatchUser(
    val name: String?,
    val email: String?,
    val phone: String?,
    val password: String?
)

data class PatchAddress(
    val street1: String?,
    val street2: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val postcode: String?,
)

fun DBUser.toCustomer(): User {
    val sorted = this.addresses.sortedBy { if (it.isDefault!!) 0 else 1 }

    return User(name!!, email, phone!!, sorted.map{it.toCustomer()})
}

fun DBAddress.toCustomer(): Address {
    return Address(
        id = id.toString(),
        street1 = street1!!,
        street2 = street2,
        city = city!!,
        state = state!!,
        country = country!!,
        postcode = postcode!!,
    )
}
