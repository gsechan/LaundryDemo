package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin

class AdminValidator(
    private val phoneValidator: PhoneValidator = PhoneValidator(),
    private val emailValidator: EmailValidator = EmailValidator(),
) {

    fun validateAdmin(admin: Admin, errors: MutableList<String>) {
        val phone = admin.phone
        if(phone == null) {
            errors.add("Phone is required")
        }
        else {
            phoneValidator.validatePhoneNumber(phone, errors)
        }

        val email = admin.email
        if(email == null) {
            errors.add("Email is required")
        }
        else {
            emailValidator.validateEmail(email, errors)
        }
    }
}
