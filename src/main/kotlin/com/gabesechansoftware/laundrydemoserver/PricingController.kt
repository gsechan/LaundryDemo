package com.gabesechansoftware.laundrydemoserver

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

//All prices in cents
data class PricesResponse(val washFold: Int, val shirts: Int, val pants: Int, val dress: Int, val suit: Int)

@RestController
class PricingController {
    @GetMapping("/prices")
    fun prices(): PricesResponse {
        return PricesResponse(100, 500, 700, 1000, 1500)
    }
}