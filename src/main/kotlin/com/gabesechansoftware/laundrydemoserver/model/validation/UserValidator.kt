package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.model.customerview.User as CustomerUser

class UserValidator(
    private val phoneValidator: PhoneValidator = PhoneValidator(),
    private val emailValidator: EmailValidator = EmailValidator(),
    private val addressValidator: AddressValidator = AddressValidator(),
) {

    fun validateUser(user: CustomerUser, errors: MutableList<String>) {
        if(user.name.length < 2) {
            errors.add("Name too short")
        }
        phoneValidator.validatePhoneNumber(user.phone, errors)
        emailValidator.validateEmail(user.email!!, errors)
        user.addresses.forEach { address ->
            addressValidator.validateCustomerAddress(address, errors)
        }
    }
}