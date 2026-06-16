package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.model.dbview.Location

class LocationValidator(
    private val addressValidator: AddressValidator = AddressValidator(),
) {

    fun validateLocation(location: Location, errors: MutableList<String>) {
        if (location.name.length < 3) {
            errors.add("Name must be at least 3 characters")
        }

        addressValidator.validateAddress(location.address, errors)
    }
}
