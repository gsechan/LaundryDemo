package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.assertNotEmpty
import kotlin.test.Test

class PasswordValidationTest {

    @Test
    fun `too short adds an error`() {
        val errors = mutableListOf<String>()
        validatePassword("1234567", errors)
        assertNotEmpty(errors)
    }

    @Test
    fun `valid password allowed`() {
        val errors = mutableListOf<String>()
        validatePassword("sdujh$(8uhNG", errors)
        assertNotEmpty(errors)
    }

}