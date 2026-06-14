package com.gabesechansoftware.laundrydemoserver.orders

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.catalog.ItemService
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadOrder
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrderRepository
import com.gabesechansoftware.laundrydemoserver.model.validation.OrderValidator
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val addressRepository: AddressRepository,
    private val itemService: ItemService,
    private val orderValidator: OrderValidator = OrderValidator(),
    private val timeSource: TimeSource = TimeSource(),
) {

    fun getAllOrders(user: User): List<Order> {
        return orderRepository.findByUser(user)
    }

    fun postUserOrder(uploadOrder: UploadOrder, authedUser: User, locale: String): Order {
        val org = authedUser.organization!!
        val now = timeSource.now()
        val errors = mutableListOf<String>()
        val pickupAddress = addressRepository.getReferenceById(UUID.fromString(uploadOrder.pickupAddress))
        val dropoffAddress = addressRepository.getReferenceById(UUID.fromString(uploadOrder.dropoffAddress))
        val order = uploadOrder.toDbOrder(
            authedUser,
            now,
            pickupAddress,
            dropoffAddress,
        )
        order.lines.addAll(uploadOrder.lines.map {
            val item = itemService.getItem(org.id, UUID.fromString(it.itemId))
            it.toDBOrderLine(item, locale, org.defaultLocale!!)
        }.toMutableList())
        orderValidator.validateOrder(order, pickupAddress, dropoffAddress, errors, true)

        if(errors.isEmpty()) {
            orderRepository.save(order)
        }
        else {
            throw APIErrorException(errors)
        }
        return order
    }
}