package com.gabesechansoftware.laundrydemoserver.services

import com.gabesechansoftware.laundrydemoserver.model.orders.Order
import com.gabesechansoftware.laundrydemoserver.repositories.OrderRepository
import org.springframework.stereotype.Service

@Service
class OrderService(private val orderRepository: OrderRepository) {
    fun getAllOrdersOfUser(user: String): List<Order> { return emptyList()}

    fun createOrder(order: Order) {
        orderRepository.save(order)
    }
}