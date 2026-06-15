package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.Order
import com.gabesechansoftware.laundrydemoserver.model.dbview.orders.OrderState
import com.gabesechansoftware.laundrydemoserver.orders.OrderService
import com.gabesechansoftware.laundrydemoserver.orders.PatchOrder
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.Runs
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
class AdminOrderControllerTest {

    @MockK
    private lateinit var orderService: OrderService

    @MockK
    private lateinit var adminAuthorizationService: AdminAuthorizationService

    @InjectMockKs
    private lateinit var controller: AdminOrderController

    private val authedAdmin = Admin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")

    private fun canEdit(value: Boolean) {
        every {
            adminAuthorizationService.permissionsCheckAny(
                listOf(AdminPermissions.EDIT_ORG, AdminPermissions.CREATE_ORG),
                authedAdmin
            )
        } returns value
    }

    @Test
    fun `listOrders - any admin can list, returns the views`() {
        val orders = listOf(Order(state = OrderState.SUBMITTED), Order(state = OrderState.COMPLETED))
        every { orderService.listAllOrders() } returns orders

        val response = controller.listOrders(authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertSize(2, response.data)
        assertEquals("SUBMITTED", response.data[0].state)
    }

    @Test
    fun `updateOrder - without edit permission returns NOT_AUTHORIZED and does not update`() {
        canEdit(false)
        val id = UUID.randomUUID()
        val patch = PatchOrder(OrderState.COMPLETED, null, null, null, null, null)

        val response = controller.updateOrder(id, PatchOrderRequest(patch), authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        assertNull(response.data)
        verify(exactly = 0) { orderService.updateOrder(any(), any()) }
    }

    @Test
    fun `updateOrder - with edit permission updates and returns the view`() {
        canEdit(true)
        val id = UUID.randomUUID()
        val patch = PatchOrder(OrderState.COMPLETED, null, null, null, null, null)
        val updated = Order(state = OrderState.COMPLETED)
        every { orderService.updateOrder(id, patch) } returns updated

        val response = controller.updateOrder(id, PatchOrderRequest(patch), authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertEquals("COMPLETED", response.data.state)
        verify { orderService.updateOrder(id, patch) }
    }

    @Test
    fun `deleteOrder - without edit permission returns NOT_AUTHORIZED and does not delete`() {
        canEdit(false)
        val id = UUID.randomUUID()

        val response = controller.deleteOrder(id, authedAdmin)

        assertEquals(NetworkErrorType.NOT_AUTHORIZED.toString(), response.errorType)
        verify(exactly = 0) { orderService.deleteOrder(any()) }
    }

    @Test
    fun `deleteOrder - with edit permission deletes and returns success`() {
        canEdit(true)
        val id = UUID.randomUUID()
        every { orderService.deleteOrder(id) } just Runs

        val response = controller.deleteOrder(id, authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        verify { orderService.deleteOrder(id) }
    }
}
