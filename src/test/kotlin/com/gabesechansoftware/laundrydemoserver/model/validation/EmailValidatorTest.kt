package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.assertEmpty
import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import kotlin.test.Test
import org.apache.commons.validator.routines.EmailValidator as ApacheValidator


class EmailValidatorTest {
    val apache = ApacheValidator.getInstance()

    val emailValidator = EmailValidator(apache)

    @Test
    fun `valid email does not add an error`() {

        val errors = mutableListOf<String>()
        emailValidator.validateEmail("gsechan@hotmail.com", errors)
        assertEmpty(errors)
    }

    @Test
    fun `invalid email adds an error`() {

        val errors = mutableListOf<String>()
        emailValidator.validateEmail("abc", errors)
        assertNotEmpty(errors)
    }

}