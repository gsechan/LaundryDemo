package com.gabesechansoftware.laundrydemoserver.controllers.catalog

import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.auth.AuthenticatedUser
import com.gabesechansoftware.laundrydemoserver.catalog.WashFoldService
import com.gabesechansoftware.laundrydemoserver.model.customerview.WashFoldPrice
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomer
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WashFoldPricesController(
    val washFoldService: WashFoldService,
) {
    @GetMapping("/washFold")
    fun washFold( @AuthenticatedUser user: User): NetworkResponse<WashFoldPrice> {
        val orgId = user.organization!!.id
        val washFoldData = washFoldService.washFoldPrice(orgId).toCustomer()
        return NetworkResponse(washFoldData)
    }
}