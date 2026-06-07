package com.gabesechansoftware.laundrydemoserver.controllers

import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.orders.ItemType
import com.gabesechansoftware.laundrydemoserver.model.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.orders.OrderLine
import com.gabesechansoftware.laundrydemoserver.model.orders.OrderState
import com.gabesechansoftware.laundrydemoserver.services.DryCleanItemService
import com.gabesechansoftware.laundrydemoserver.services.OrderService
import com.gabesechansoftware.laundrydemoserver.services.WashFoldService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

data class PostOrderRequest(
    val lines: List<PostOrderLine>,
    val scheduledPickup: Long,
    val scheduledDropoff: Long
)

data class PostOrderLine(
    val itemId: String,
    val quantity: String?,
    val itemType: String,
)

data class PostOrderResponse(val success: Boolean, val orderId: String)

@RestController
class OrderController(
    @Autowired val orderService: OrderService,
    @Autowired val loginAuthenticator: LoginAuthenticator,
    @Autowired val washFoldService: WashFoldService,
    @Autowired val dryCleanItemService: DryCleanItemService,
) {

    @PostMapping("/orders")
    fun newOrder(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody request: PostOrderRequest,
        @RequestHeader("Accept-Language") locale: String,
    ): PostOrderResponse {
        val token = authHeader.substringAfter(" ")
        val authedUser = loginAuthenticator.authenticateToken(token)
        val org = authedUser.organization!!

        val now = OffsetDateTime.now(ZoneOffset.UTC)

        if(request.lines.isEmpty()) {
            throw IllegalStateException("Must have at least 1 line")
        }
        val order = Order().apply {
            user = authedUser
            state = OrderState.SUBMITTED
            submitted = now
            lastChange = now
            completed = null
            scheduledPickup =  Instant.ofEpochMilli(request.scheduledPickup).atOffset(ZoneOffset.UTC)
            scheduledDropoff =  Instant.ofEpochMilli(request.scheduledDropoff).atOffset(ZoneOffset.UTC)

            lines = request.lines.map { requestLine ->
                val requestItemType = enumValueOf<ItemType>(requestLine.itemType)
                var pricePerUnit: BigDecimal
                var quantity: BigDecimal?
                var totalCost: BigDecimal?
                var nameInSubmitLocale: String?
                var nameInOrgsLocale: String?
                var nameInDefaultLocale: String?
                if (requestItemType == ItemType.WASH_AND_FOLD) {
                    pricePerUnit = washFoldService.washFoldPrice(org.id!!).price
                    if (requestLine.quantity != null) {
                        throw IllegalStateException("Wash and fold must not have quantity")
                    }
                    quantity = null
                    totalCost = null
                    nameInSubmitLocale = "Wash and fold"
                    nameInOrgsLocale = "Wash and fold"
                    nameInDefaultLocale = "Wash and fold"

                } else if (requestItemType == ItemType.DRY_CLEANING) {
                    if (requestLine.quantity == null) {
                        throw IllegalStateException("DCI must have quantity")
                    }
                    val dryCleanItem =
                        dryCleanItemService.getDryCleanItem(org.id!!, UUID.fromString(requestLine.itemId))
                    pricePerUnit = dryCleanItem.price!!
                    quantity = BigDecimal(requestLine.quantity)
                    totalCost = quantity.times(pricePerUnit)

                    nameInSubmitLocale = dryCleanItemService.findMatchingNameForItem(dryCleanItem.names, locale)
                    nameInOrgsLocale =
                        dryCleanItemService.findMatchingNameForItem(dryCleanItem.names, org.defaultLocale!!)
                    nameInDefaultLocale = dryCleanItemService.findMatchingNameForItem(dryCleanItem.names, "en-US")
                } else {
                    throw IllegalStateException("Unknown item")
                }
                OrderLine().apply {
                    itemType = requestItemType
                    this.totalCost = totalCost
                    this.quantity = quantity
                    this.pricePerUnit = pricePerUnit

                    submittedLocale = locale
                    nameInSubmittedLocale = nameInSubmitLocale
                    nameInEnglishLocale = nameInDefaultLocale
                    nameInOrgLocale = nameInOrgsLocale

                    orgLocale = org.defaultLocale


                }
            }.toSet()
        }
        orderService.createOrder(order)
        return PostOrderResponse(true, order.id.toString())
    }

}