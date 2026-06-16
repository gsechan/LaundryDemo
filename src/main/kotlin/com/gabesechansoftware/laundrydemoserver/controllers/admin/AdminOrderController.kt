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
import com.gabesechansoftware.laundrydemoserver.model.adminview.AdminOrderView
import com.gabesechansoftware.laundrydemoserver.model.adminview.toAdminView


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

    @GetMapping("/admin/organizations/{orgId}/orders")
    fun listOrders(
        @PathVariable orgId: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<List<AdminOrderView>> {
        return NetworkResponse(orderService.listOrdersByOrg(orgId).map { it.toAdminView() })
    }

    @GetMapping("/admin/organizations/{orgId}/orders/{id}")
    fun getOrder(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<AdminOrderView> {
        return NetworkResponse(orderService.getOrder(id).toAdminView())
    }

    @PatchMapping("/admin/organizations/{orgId}/orders/{id}")
    fun updateOrder(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: PatchOrderRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<AdminOrderView> {
        if (!canEditOrg(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to edit orders")
        }
        return NetworkResponse(orderService.updateOrder(id, request.order).toAdminView())
    }

    @DeleteMapping("/admin/organizations/{orgId}/orders/{id}")
    fun deleteOrder(
        @PathVariable orgId: UUID,
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
