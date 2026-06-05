package com.gabesechansoftware.laundrydemoserver.controllers

import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.services.DryCleanItemService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class DryCleanItemsResponse(
    val items: List<JSONDryCleanItem>
)

data class JSONDryCleanItem(
    val id: String,
    val name: String,
    val price: String
)

@RestController
class DryCleanItemController(
    @Autowired val dryCleanItemService: DryCleanItemService,
    @Autowired val loginAuthenticator: LoginAuthenticator,
) {
    @GetMapping("/dryCleanItem")
    fun dryCleanItem(
        @RequestHeader("Authorization") authHeader: String,
        @RequestHeader("Accept-Language") locale: String,
    ): DryCleanItemsResponse {
        val token = authHeader.substringAfter(" ")
        val orgId = loginAuthenticator.authenticateToken(token).organization?.id!!
        val  items = dryCleanItemService.getDryCleanItems(orgId, locale)
        return DryCleanItemsResponse(items.map { JSONDryCleanItem(it.id.toString(), it.name, it.price.toString()) })
    }
}