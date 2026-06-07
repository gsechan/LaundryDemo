package com.gabesechansoftware.laundrydemoserver.repositories

import com.gabesechansoftware.laundrydemoserver.model.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID


interface OrderRepository: JpaRepository<Order, UUID> {
    fun findByUser(user: User): List<Order>
}