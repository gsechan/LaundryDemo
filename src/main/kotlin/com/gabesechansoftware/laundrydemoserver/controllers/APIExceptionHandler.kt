package com.gabesechansoftware.laundrydemoserver.controllers

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class APIExceptionHandler {
    @ExceptionHandler(APIErrorException::class)
    fun handleAPIError(ex: APIErrorException): ResponseEntity<NetworkResponse<Unit>> {
        return ResponseEntity.status(HttpStatus.OK).body(NetworkResponse(NetworkErrorType.API_SPECIFIC_ERROR, ex.errors))
    }

}