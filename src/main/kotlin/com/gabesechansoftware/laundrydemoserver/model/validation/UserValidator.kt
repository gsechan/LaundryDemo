package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User

class UserValidator(
    private val phoneValidator: PhoneValidator = PhoneValidator(),
    private val emailValidator: EmailValidator = EmailValidator(),
    private val addressValidator: AddressValidator = AddressValidator(),
) {

    fun validateUser(user: User, errors: MutableList<String>) {
        val name = user.name
        if(name == null) {
            errors.add("Name is required")
        }
        else if(name.length  < 2) {
            errors.add( "Name too short")
        }
        val phone = user.phone
        if(phone == null) {
            errors.add("Phone is required")
        }
        else {
            phoneValidator.validatePhoneNumber(phone, errors)
        }
        val email = user.email
        //Email allowed to be missing
        if(email != null) {
            emailValidator.validateEmail(email, errors)
        }
        val organization = user.organization
        if(organization == null) {
            errors.add("Organization is required")
        }

        user.addresses.forEach { address ->
            addressValidator.validateAddress(address, errors)
        }

        //If this constraint is changed, must also change in UserService
        if(user.addresses.size > 5) {
            errors.add("Too many addresses")
        }


    }
}