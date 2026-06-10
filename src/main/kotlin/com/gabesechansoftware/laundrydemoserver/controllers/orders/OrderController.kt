package com.gabesechansoftware.laundrydemoserver.controllers.orders

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.ItemType
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderLine
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderState
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.catalog.DryCleanItemService
import com.gabesechansoftware.laundrydemoserver.services.OrderService
import com.gabesechansoftware.laundrydemoserver.services.WashFoldService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
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
    val scheduledDropoff: Long,
    val pickupAddress: String,
    val dropoffAddress: String,
)

data class PostOrderLine(
    val itemId: String,
    val quantity: String?,
    val itemType: String,
)

data class PostOrderResponse(val success: Boolean, val orderId: String)
data class GetOrderResponse(val orders:List<GetOrder>)

data class GetOrder(
    val id: String,
    val state: String,
    val completed: Long?,
    val lastChange: Long,
    val submitted: Long,
    val scheduledPickup: Long,
    val scheduledDropoff: Long,
    val pickupAddressId: String,
    val dropoffAddressId: String,
    val lines: List<GetOrderLine>
)

data class GetOrderLine(
    val id: String,
    val itemType: String,
    val name: String,
    val price_per_unit: String,
    val quantity: String?,
    val total_cost: String?,
)

@RestController
class OrderController(
    @Autowired val orderService: OrderService,
    @Autowired val loginAuthenticator: LoginAuthenticator,
    @Autowired val washFoldService: WashFoldService,
    @Autowired val dryCleanItemService: DryCleanItemService,
    @Autowired val addressRepository: AddressRepository,
) {

    @PostMapping("/orders")
    fun newOrder(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody request: PostOrderRequest,
        @RequestHeader("Accept-Language") locale: String,
    ): NetworkResponse<PostOrderResponse> {
        val org: Organization
        val authedUser: User
        try {
            val token = authHeader.substringAfter(" ")
            authedUser = loginAuthenticator.authenticateToken(token)
            org = authedUser.organization!!
        }
        catch (e: Exception) {
            e.printStackTrace()
            return NetworkResponse(NetworkErrorType.BAD_AUTH, "Token invalid")
        }

        val now = OffsetDateTime.now(ZoneOffset.UTC)

        if (request.lines.isEmpty()) {
            return NetworkResponse(
                NetworkErrorType.API_SPECIFIC_ERROR,
                "There must be at least one line in an order"
            )
        }
        val order = Order().apply {
            user = authedUser
            state = OrderState.SUBMITTED
            submitted = now
            lastChange = now
            completed = null
            scheduledPickup = Instant.ofEpochMilli(request.scheduledPickup).atOffset(ZoneOffset.UTC)
            scheduledDropoff = Instant.ofEpochMilli(request.scheduledDropoff).atOffset(ZoneOffset.UTC)
            pickupAddress = addressRepository.getReferenceById(UUID.fromString(request.pickupAddress))
            dropoffAddress = addressRepository.getReferenceById(UUID.fromString(request.dropoffAddress))

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
                        return NetworkResponse(
                            NetworkErrorType.API_SPECIFIC_ERROR,
                            "Wash and Fold lines must not have a quantity"
                        )
                    }
                    quantity = null
                    totalCost = null
                    nameInSubmitLocale = "Wash and fold"
                    nameInOrgsLocale = "Wash and fold"
                    nameInDefaultLocale = "Wash and fold"

                } else if (requestItemType == ItemType.DRY_CLEANING) {
                    if (requestLine.quantity == null) {
                        return NetworkResponse(
                            NetworkErrorType.API_SPECIFIC_ERROR,
                            "Dry cleaning lines must have a quantity"
                        )
                    }
                    val dryCleanItem =
                        dryCleanItemService.getDryCleanItem(org.id!!, UUID.fromString(requestLine.itemId))
                    pricePerUnit = dryCleanItem.price!!
                    quantity = BigDecimal(requestLine.quantity)
                    totalCost = quantity.times(pricePerUnit)

                    nameInSubmitLocale = dryCleanItemService.getDryCleanItemNameForLocale(dryCleanItem, locale)
                    nameInOrgsLocale = dryCleanItemService.getDryCleanItemNameForLocale(dryCleanItem, org.defaultLocale!!)
                    nameInDefaultLocale = dryCleanItemService.getDryCleanItemNameForLocale(dryCleanItem, "en-US")
                } else {
                    return NetworkResponse(
                        NetworkErrorType.API_SPECIFIC_ERROR,
                        "Unknown item type"
                    )

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
            }.toMutableList()
        }
        orderService.createOrder(order)
        return NetworkResponse(PostOrderResponse(true, order.id.toString()))
    }

    @GetMapping("/orders")
    fun allOrders(
        @RequestHeader("Authorization") authHeader: String,
    ): NetworkResponse<GetOrderResponse> {
        val authedUser: User
        try {
            val token = authHeader.substringAfter(" ")
            authedUser = loginAuthenticator.authenticateToken(token)
        }
        catch (e: Exception) {
            e.printStackTrace()
            return NetworkResponse(NetworkErrorType.BAD_AUTH, "Token invalid")
        }
        val orders = orderService.getAllOrdersOfUser(authedUser)
        return NetworkResponse(
            GetOrderResponse(
                orders.map { order ->
                    GetOrder(
                        order.id.toString(),
                        order.state.toString(),
                        order.completed?.toInstant()?.toEpochMilli(),
                        order.lastChange!!.toInstant().toEpochMilli(),
                        order.submitted!!.toInstant().toEpochMilli(),
                        order.scheduledPickup!!.toInstant().toEpochMilli(),
                        order.scheduledDropoff!!.toInstant().toEpochMilli(),
                        order.pickupAddress!!.id.toString(),
                        order.dropoffAddress!!.id.toString(),
                        order.lines!!.map { line ->
                            GetOrderLine(
                                line.id.toString(),
                                line.itemType.toString(),
                                line.nameInSubmittedLocale ?: line.nameInEnglishLocale ?: "Unknown Item",
                                line.pricePerUnit.toString(),
                                line.quantity?.toString(),
                                line.totalCost?.toString()
                            )
                        }
                    )
                }
            )
        )
    }
}