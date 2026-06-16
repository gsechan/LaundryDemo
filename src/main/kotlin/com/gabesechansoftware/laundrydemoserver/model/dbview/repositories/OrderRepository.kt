package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories

import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID


interface OrderRepository: JpaRepository<Order, UUID> {
    fun findByUser(user: User): List<Order>
    fun findByUserOrganizationId(organizationId: UUID): List<Order>
}