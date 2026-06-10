package com.gabesechansoftware.laundrydemoserver.model.validation

import org.springframework.stereotype.Component
import org.apache.commons.validator.routines.EmailValidator as ApacheValidator

@Component
class EmailValidator(private val validator: ApacheValidator = ApacheValidator.getInstance()) {

    fun validateEmail(mail: String, errors: MutableList<String>) {
        if(!validator.isValid(mail)) {
            errors.add("Email is not valid")
        }
    }
}