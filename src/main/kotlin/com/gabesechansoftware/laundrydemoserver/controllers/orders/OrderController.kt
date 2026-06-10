package com.gabesechansoftware.laundrydemoserver.controllers.orders

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.auth.AuthenticatedUser
import com.gabesechansoftware.laundrydemoserver.model.customerview.Order as CustomerOrder
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadOrder
import com.gabesechansoftware.laundrydemoserver.orders.OrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController


data class PostOrderRequest(
    val order: UploadOrder
)


data class PostOrderResponse(val success: Boolean, val orderId: String)

@RestController
class OrderController(
    val orderService: OrderService,
) {

    @PostMapping("/orders")
    fun newOrder(
        @AuthenticatedUser authedUser: User,
        @RequestBody request: PostOrderRequest,
        @RequestHeader("Accept-Language") locale: String,
    ): NetworkResponse<PostOrderResponse> {
        try {
            val order = orderService.postUserOrder(request.order, authedUser, locale)
            return NetworkResponse(PostOrderResponse(true, order.id.toString()))
        }
        catch (ex: APIErrorException) {
            return NetworkResponse(NetworkErrorType.API_SPECIFIC_ERROR, ex.errors)
        }
    }

    @GetMapping("/orders")
    fun allOrders(
        @AuthenticatedUser authedUser: User,
    ): NetworkResponse<List<CustomerOrder>> {
        val orders = orderService.getAllOrdersForCustomerView(authedUser)
        return NetworkResponse(orders)
    }
}