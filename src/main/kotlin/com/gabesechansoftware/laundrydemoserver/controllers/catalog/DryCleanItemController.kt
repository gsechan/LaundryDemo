package com.gabesechansoftware.laundrydemoserver.controllers.catalog

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.catalog.DryCleanItemService
import com.gabesechansoftware.laundrydemoserver.model.customerview.DryCleanItem
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


data class DryCleanItemsResponse(
    val items: List<DryCleanItem>
)


@RestController
class DryCleanItemController(
    private val dryCleanItemService: DryCleanItemService,
    private val loginAuthenticator: LoginAuthenticator,
) {
    @GetMapping("/dryCleanItem")
    fun dryCleanItem(
        @RequestHeader("Authorization") authHeader: String,
        @RequestHeader("Accept-Language") locale: String,
    ): NetworkResponse<DryCleanItemsResponse> {
        val orgId: UUID
        try {
            val token = authHeader.substringAfter(" ")
            orgId = loginAuthenticator.authenticateToken(token).organization?.id!!
        }
        catch (e: Exception) {
            e.printStackTrace()
            return NetworkResponse(NetworkErrorType.BAD_AUTH, "Token invalid")
        }
        val  items = dryCleanItemService.getCustomerDryCleanItems(orgId, locale)
        return NetworkResponse(DryCleanItemsResponse(items))
    }
}