package com.gabesechansoftware.laundrydemoserver.repositories

import com.gabesechansoftware.laundrydemoserver.model.orders.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID


interface OrderRepository: JpaRepository<Order, UUID> {
    @Query("SELECT o FROM Order o ORDER BY o.completed DESC NULLS FIRST")
    fun findByUserId(userId: UUID): List<Order>
}