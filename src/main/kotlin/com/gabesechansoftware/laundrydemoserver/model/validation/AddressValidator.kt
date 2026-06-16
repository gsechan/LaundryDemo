package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.model.dbview.EmbeddedAddress
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address

class AddressValidator {

    fun validateAddress(address: EmbeddedAddress, errors: MutableList<String>) {}
    fun validateAddress(address: Address, errors: MutableList<String>) {}

}