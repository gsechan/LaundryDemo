package com.gabesechansoftware.laundrydemoserver.controllers.catalog

import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.authentication.AuthenticatedUser
import com.gabesechansoftware.laundrydemoserver.catalog.ItemService
import com.gabesechansoftware.laundrydemoserver.model.customerview.Item
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomer
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class ItemsResponse(
    val items: List<Item>
)

@RestController
class ItemController(
    private val itemService: ItemService,
) {
    @GetMapping("/items")
    fun getItems(
        @AuthenticatedUser user: User,
        @RequestHeader("Accept-Language") locale: String,
        @RequestParam(required = false) addressId: UUID?,
    ): NetworkResponse<ItemsResponse> {
        val items = itemService.getItemsForUser(user, addressId).map { it.toCustomer(locale) }
        return NetworkResponse(ItemsResponse(items))
    }
}
