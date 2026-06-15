package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.users.UserService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class AdminUserControllerTest {

    @MockK
    private lateinit var userService: UserService

    @InjectMockKs
    private lateinit var controller: AdminUserController

    private val authedAdmin = Admin(name = "Gabe", email = "admin@provider.com", phone = "3128675309")

    @Test
    fun `listUsers - returns the user views for the organization`() {
        val orgId = UUID.randomUUID()
        val users = listOf(
            User(name = "Gabe", email = "gabe@example.com", phone = "3128675309"),
            User(name = "Sue", email = "sue@example.com", phone = "2065551212"),
        )
        every { userService.listByOrganization(orgId) } returns users

        val response = controller.listUsers(orgId, authedAdmin)

        assertEquals(NetworkErrorType.NONE.toString(), response.errorType)
        assertNotNull(response.data)
        assertSize(2, response.data)
        assertEquals("Gabe", response.data[0].name)
        assertEquals("gabe@example.com", response.data[0].email)
    }
}
