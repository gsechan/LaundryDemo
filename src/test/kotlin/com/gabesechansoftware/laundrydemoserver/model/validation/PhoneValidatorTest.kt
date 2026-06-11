package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.assertEmpty
import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlin.test.Test


class PhoneValidatorTest {
    val phone = PhoneNumberUtil.getInstance()!!

    val phoneValidator = PhoneValidator(phone)

    @Test
    fun `valid phone does not add an error`() {

        val errors = mutableListOf<String>()
        phoneValidator.validatePhoneNumber("2067140469", errors)
        assertEmpty(errors)
    }

    @Test
    fun `invalid phone adds an error`() {

        val errors = mutableListOf<String>()
        phoneValidator.validatePhoneNumber("5", errors)
        assertNotEmpty(errors)
    }

}