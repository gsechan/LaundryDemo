package com.gabesechansoftware.laundrydemoserver.orders

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.catalog.DryCleanItemService
import com.gabesechansoftware.laundrydemoserver.catalog.WashFoldService
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadOrder
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomer
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.ItemType
import com.gabesechansoftware.laundrydemoserver.model.customerview.Order as CustomerOrder
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderLine
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderState
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrderRepository
import com.gabesechansoftware.laundrydemoserver.model.validation.OrderValidator
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val addressRepository: AddressRepository,
    private val washFoldService: WashFoldService,
    private val dryCleanItemService: DryCleanItemService,
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
        orderValidator.validateUploadOrder(uploadOrder, errors)
        val order = Order().apply {
            user = authedUser
            state = OrderState.SUBMITTED
            submitted = now
            lastChange = now
            completed = null
            scheduledPickup = Instant.ofEpochMilli(uploadOrder.scheduledPickup).atOffset(ZoneOffset.UTC)
            scheduledDropoff = Instant.ofEpochMilli(uploadOrder.scheduledDropoff).atOffset(ZoneOffset.UTC)
            pickupAddress = addressRepository.getReferenceById(UUID.fromString(uploadOrder.pickupAddress))
            dropoffAddress = addressRepository.getReferenceById(UUID.fromString(uploadOrder.dropoffAddress))

            lines = uploadOrder.lines.map { requestLine ->
                val requestItemType = enumValueOf<ItemType>(requestLine.itemType)
                var pricePerUnit = BigDecimal.ZERO
                var quantity: BigDecimal? = null
                var totalCost: BigDecimal? = null
                var nameInSubmitLocale: String? = null
                var nameInOrgsLocale: String? = null
                var nameInDefaultLocale: String? = null
                when(requestItemType) {
                    ItemType.WASH_AND_FOLD -> {
                        pricePerUnit = washFoldService.washFoldPriceInternal(org.id).price!!
                        quantity = null
                        totalCost = null
                        nameInSubmitLocale = "Wash and fold"
                        nameInOrgsLocale = "Wash and fold"
                        nameInDefaultLocale = "Wash and fold"
                    }

                    ItemType.DRY_CLEANING -> {
                        val dryCleanItem = dryCleanItemService.getDryCleanItem(
                            org.id,
                            UUID.fromString(requestLine.itemId)
                        )
                        pricePerUnit = dryCleanItem.price!!
                        quantity = BigDecimal(requestLine.quantity)
                        totalCost = quantity.times(pricePerUnit)

                        nameInSubmitLocale = dryCleanItemService.getDryCleanItemNameForLocale(dryCleanItem, locale)
                        nameInOrgsLocale =
                            dryCleanItemService.getDryCleanItemNameForLocale(dryCleanItem, org.defaultLocale!!)
                        nameInDefaultLocale = dryCleanItemService.getDryCleanItemNameForLocale(dryCleanItem, "en-US")
                    }
                    else -> {
                        errors.add("Unknown item type")
                    }
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
        if(errors.isEmpty()) {
            orderRepository.save(order)
        }
        else {
            throw APIErrorException(errors)
        }
        return order
    }
}