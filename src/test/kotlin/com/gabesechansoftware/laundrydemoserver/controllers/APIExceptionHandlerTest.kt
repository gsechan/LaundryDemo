package com.gabesechansoftware.laundrydemoserver.controllers

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import org.springframework.http.HttpStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class APIExceptionHandlerTest {

    private val handler = APIExceptionHandler()

    @Test
    fun `handleAPIError for APIErrorException returns errors and OK status`() {
        val errors = listOf("Error 1", "Error 2")
        val exception = APIErrorException(errors)

        val result = handler.handleAPIError(exception)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(false, result.body!!.success)
        assertEquals(NetworkErrorType.API_SPECIFIC_ERROR.toString(), result.body!!.errorType)
        assertEquals(errors, result.body!!.errors)
        assertEquals(null, result.body!!.data)
    }

    @Test
    fun `handleAPIError for EntityDoesNotExistException returns message and OK status`() {
        val exception = EntityDoesNotExistException("Item does not exist")

        val result = handler.handleAPIError(exception)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(false, result.body!!.success)
        assertEquals(NetworkErrorType.ENTITY_DOES_NOT_EXIST.toString(), result.body!!.errorType)
        assertEquals(listOf("Item does not exist"), result.body!!.errors)
        assertEquals(null, result.body!!.data)
    }
}
