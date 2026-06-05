package com.gabesechansoftware.laundrydemoserver.controllers

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
    fun washFold( @RequestHeader("Authorization") authHeader: String): WashFoldResponse {
        val token = authHeader.substringAfter(" ")
        val orgId = loginAuthenticator.authenticateToken(token).organization?.id!!
        val washFoldData = washFoldService.washFoldPrice(orgId)
        return WashFoldResponse(washFoldData.price.toString(), washFoldData.avgWeight.toString())
    }
}