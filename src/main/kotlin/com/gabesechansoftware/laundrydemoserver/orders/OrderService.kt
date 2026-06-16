package com.gabesechansoftware.laundrydemoserver.orders

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.catalog.ItemService
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadOrder
import com.gabesechansoftware.laundrydemoserver.model.dbview.EmbeddedAddress
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderState
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrderRepository
import com.gabesechansoftware.laundrydemoserver.model.validation.OrderValidator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

data class PatchOrderAddress(
    val street1: String?,
    val street2: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val postcode: String?,
)

data class PatchOrderLine(
    val id: UUID,
    val quantity: String?,
)

data class PatchOrder(
    val state: OrderState?,
    val scheduledPickup: Long?,
    val scheduledDropoff: Long?,
    val pickupAddress: PatchOrderAddress?,
    val dropoffAddress: PatchOrderAddress?,
    val lines: List<PatchOrderLine>?,
)

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

    fun listAllOrders(): List<Order> {
        return orderRepository.findAll()
    }

    @Transactional
    fun updateOrder(orderId: UUID, patch: PatchOrder): Order {
        val order = orderRepository.findById(orderId)
            .orElseThrow { EntityDoesNotExistException("Order $orderId does not exist") }

        patch.state?.let { order.state = it }
        patch.scheduledPickup?.let { order.scheduledPickup = Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC) }
        patch.scheduledDropoff?.let { order.scheduledDropoff = Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC) }

        patch.pickupAddress?.let { a ->
            val existing = order.pickupAddress
            order.pickupAddress = EmbeddedAddress(
                street1 = a.street1 ?: existing?.street1 ?: "",
                street2 = a.street2 ?: existing?.street2,
                city = a.city ?: existing?.city ?: "",
                state = a.state ?: existing?.state ?: "",
                country = a.country ?: existing?.country ?: "",
                postcode = a.postcode ?: existing?.postcode ?: "",
            )
        }
        patch.dropoffAddress?.let { a ->
            val existing = order.dropoffAddress
            order.dropoffAddress = EmbeddedAddress(
                street1 = a.street1 ?: existing?.street1 ?: "",
                street2 = a.street2 ?: existing?.street2,
                city = a.city ?: existing?.city ?: "",
                state = a.state ?: existing?.state ?: "",
                country = a.country ?: existing?.country ?: "",
                postcode = a.postcode ?: existing?.postcode ?: "",
            )
        }

        patch.lines?.forEach { linePatch ->
            val line = order.lines.find { it.id == linePatch.id }
                ?: throw EntityDoesNotExistException("Order line ${linePatch.id} does not exist on this order")
            linePatch.quantity?.let { q ->
                val quantity = try {
                    BigDecimal(q)
                } catch (e: NumberFormatException) {
                    throw APIErrorException(listOf("Invalid quantity"))
                }
                line.quantity = quantity
                // totalCost is never set directly; it is always derived.
                line.totalCost = line.pricePerUnit?.times(quantity)
            }
        }

        order.lastChange = timeSource.now()

        val errors = mutableListOf<String>()
        orderValidator.validateEditableFields(order, errors)
        if(errors.isNotEmpty()) {
            throw APIErrorException(errors)
        }

        orderRepository.save(order)
        return order
    }

    @Transactional
    fun deleteOrder(orderId: UUID) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { EntityDoesNotExistException("Order $orderId does not exist") }
        orderRepository.delete(order)
    }

    fun postUserOrder(uploadOrder: UploadOrder, authedUser: User, locale: String): Order {
        val org = authedUser.organization!!
        val now = timeSource.now()
        val errors = mutableListOf<String>()
        val pickupAddress = addressRepository.getReferenceById(UUID.fromString(uploadOrder.pickupAddress))
        val dropoffAddress = addressRepository.getReferenceById(UUID.fromString(uploadOrder.dropoffAddress))
        val order = uploadOrder.toDbOrder(authedUser, now, pickupAddress, dropoffAddress)
        order.lines.addAll(uploadOrder.lines.map {
            val item = itemService.getItem(org.id, UUID.fromString(it.itemId))
            it.toDBOrderLine(item, locale, org.defaultLocale!!)
        }.toMutableList())
        orderValidator.validateOrder(order, errors, true)

        if(errors.isEmpty()) {
            orderRepository.save(order)
        }
        else {
            throw APIErrorException(errors)
        }
        return order
    }
}