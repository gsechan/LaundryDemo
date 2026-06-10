package com.gabesechansoftware.laundrydemoserver.controllers.catalog

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.catalog.WashFoldService
import com.gabesechansoftware.laundrydemoserver.model.customerview.WashFoldPrice
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class WashFoldPricesController(
    val washFoldService: WashFoldService,
    val loginAuthenticator: LoginAuthenticator,
) {
    @GetMapping("/washFold")
    fun washFold( @RequestHeader("Authorization") authHeader: String): NetworkResponse<WashFoldPrice> {
        val orgId: UUID
        try {
            val token = authHeader.substringAfter(" ")
            orgId = loginAuthenticator.authenticateToken(token).organization?.id!!
        }
        catch (e: Exception) {
            e.printStackTrace()
            return NetworkResponse(NetworkErrorType.BAD_AUTH, "Token invalid")
        }
        val washFoldData = washFoldService.washFoldPriceForCustomer(orgId)
        return NetworkResponse(washFoldData)
    }
}