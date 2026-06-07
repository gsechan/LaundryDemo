package com.gabesechansoftware.laundrydemoserver.controllers

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.services.WashFoldService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class WashFoldResponse(val price: String, val avgWeight: String)

@RestController
class WashFoldPricesController(
    @Autowired val washFoldService: WashFoldService,
    @Autowired val loginAuthenticator: LoginAuthenticator,
) {
    @GetMapping("/washFold")
    fun washFold( @RequestHeader("Authorization") authHeader: String): NetworkResponse<WashFoldResponse> {
        val orgId: UUID
        try {
            val token = authHeader.substringAfter(" ")
            orgId = loginAuthenticator.authenticateToken(token).organization?.id!!
        }
        catch (e: Exception) {
            e.printStackTrace()
            return NetworkResponse(NetworkErrorType.BAD_AUTH, "Token invalid")
        }
        val washFoldData = washFoldService.washFoldPrice(orgId)
        return NetworkResponse(
            WashFoldResponse(
                washFoldData.price.toString(),
                washFoldData.avgWeight.toString()
            )
        )
    }
}