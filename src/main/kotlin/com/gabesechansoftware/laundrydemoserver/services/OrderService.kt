package com.gabesechansoftware.laundrydemoserver.services

import com.gabesechansoftware.laundrydemoserver.model.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.user.User
import com.gabesechansoftware.laundrydemoserver.repositories.OrderRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OrderService(private val orderRepository: OrderRepository) {
    fun getAllOrdersOfUser(user: User): List<Order> {
        return orderRepository.findByUser(user)
    }

    fun createOrder(order: Order) {
        orderRepository.save(order)
    }

}