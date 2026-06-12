package com.gabesechansoftware.laundrydemoserver.controllers.catalog

import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.auth.AuthenticatedUser
import com.gabesechansoftware.laundrydemoserver.catalog.DryCleanItemService
import com.gabesechansoftware.laundrydemoserver.model.customerview.DryCleanItem
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomer
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController


data class DryCleanItemsResponse(
    val items: List<DryCleanItem>
)


@RestController
class DryCleanItemController(
    private val dryCleanItemService: DryCleanItemService,
) {
    @GetMapping("/dryCleanItem")
    fun dryCleanItem(
        @AuthenticatedUser user: User,
        @RequestHeader("Accept-Language") locale: String,
    ): NetworkResponse<DryCleanItemsResponse> {
        val  items = dryCleanItemService.getDryCleanItems(user.organization!!.id, locale).map { it.toCustomer(locale) }
        return NetworkResponse(DryCleanItemsResponse(items))
    }
}