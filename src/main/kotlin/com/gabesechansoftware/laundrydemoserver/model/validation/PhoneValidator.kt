package com.gabesechansoftware.laundrydemoserver.model.validation

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import org.springframework.stereotype.Component

@Component
class PhoneValidator(private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()) {

    fun validatePhoneNumber(phoneNumber: String, errors: MutableList<String>) {
        try {
            // "US" acts as a default region fallback if no explicit prefix (like +44) is found
            val number: PhoneNumber? = phoneNumberUtil.parse(phoneNumber, "US")
            if(!phoneNumberUtil.isValidNumber(number)) {
                errors.add("Invalid phone number")
            }
        } catch (e: Exception) {
            errors.add("Invalid phone number")
        }
    }

}