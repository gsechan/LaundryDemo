package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.authentication.AuthenticatedAdmin
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order
import com.gabesechansoftware.laundrydemoserver.orders.OrderService
import com.gabesechansoftware.laundrydemoserver.orders.PatchOrder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


data class OrderAddressView(
    val street1: String?,
    val street2: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val postcode: String?,
)

data class AdminOrderLineView(
    val id: String,
    val name: String?,
    val itemType: String?,
    val pricePerUnit: String?,
    val quantity: String?,
    val totalCost: String?,
)

data class AdminOrderView(
    val id: String,
    val userId: String?,
    val state: String?,
    val submitted: Long?,
    val lastChange: Long?,
    val completed: Long?,
    val scheduledPickup: Long?,
    val scheduledDropoff: Long?,
    val pickupAddress: OrderAddressView,
    val dropoffAddress: OrderAddressView,
    val lines: List<AdminOrderLineView>,
)

fun Order.toAdminView() = AdminOrderView(
    id = id.toString(),
    userId = user?.id?.toString(),
    state = state?.name,
    submitted = submitted?.toInstant()?.toEpochMilli(),
    lastChange = lastChange?.toInstant()?.toEpochMilli(),
    completed = completed?.toInstant()?.toEpochMilli(),
    scheduledPickup = scheduledPickup?.toInstant()?.toEpochMilli(),
    scheduledDropoff = scheduledDropoff?.toInstant()?.toEpochMilli(),
    pickupAddress = OrderAddressView(pickupStreet1, pickupStreet2, pickupCity, pickupState, pickupCountry, pickupPostcode),
    dropoffAddress = OrderAddressView(dropoffStreet1, dropoffStreet2, dropoffCity, dropoffState, dropoffCountry, dropoffPostcode),
    lines = lines.map {
        AdminOrderLineView(
            id = it.id.toString(),
            name = it.nameInEnglishLocale ?: it.nameInSubmittedLocale,
            itemType = it.itemType?.name,
            pricePerUnit = it.pricePerUnit?.toString(),
            quantity = it.quantity?.toString(),
            totalCost = it.totalCost?.toString(),
        )
    },
)

data class PatchOrderRequest(val order: PatchOrder)


@RestController
class AdminOrderController(
    private val orderService: OrderService,
    private val adminAuthorizationService: AdminAuthorizationService,
) {

    private fun canEditOrg(admin: Admin) =
        adminAuthorizationService.permissionsCheckAny(
            listOf(AdminPermissions.EDIT_ORG, AdminPermissions.CREATE_ORG),
            admin
        )

    @GetMapping("/admin/orders")
    fun listOrders(
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<List<AdminOrderView>> {
        return NetworkResponse(orderService.listAllOrders().map { it.toAdminView() })
    }

    @PatchMapping("/admin/orders/{id}")
    fun updateOrder(
        @PathVariable id: UUID,
        @RequestBody request: PatchOrderRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<AdminOrderView> {
        if (!canEditOrg(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to edit orders")
        }
        return NetworkResponse(orderService.updateOrder(id, request.order).toAdminView())
    }

    @DeleteMapping("/admin/orders/{id}")
    fun deleteOrder(
        @PathVariable id: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<Unit> {
        if (!canEditOrg(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to delete orders")
        }
        orderService.deleteOrder(id)
        return NetworkResponse(Unit)
    }
}
