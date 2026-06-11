package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import kotlin.test.Test

class PasswordValidationTest {

    val passwordValidator  = PasswordValidator()
    
    @Test
    fun `too short adds an error`() {
        val errors = mutableListOf<String>()
        passwordValidator.validatePassword("1234567", errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `valid password allowed`() {
        val errors = mutableListOf<String>()
        passwordValidator.validatePassword("sdujh$(8uhNG", errors)
        assertNotEmpty(errors)
    }

}