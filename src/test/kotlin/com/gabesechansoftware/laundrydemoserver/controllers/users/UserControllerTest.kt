package com.gabesechansoftware.laundrydemoserver.controllers.users

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.TimeSource
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.customerview.PatchUser
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadUser
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Session
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.users.UserService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class UserControllerTest {

    @MockK
    lateinit var userService: UserService

    @MockK
    lateinit var loginAuthenticator: LoginAuthenticator

    @InjectMockKs
    lateinit var userController: UserController

    @Test
    fun `createUser returns converted service response`() {
        val org = Organization()
        val uploadUser = UploadUser("Gabe", "test@example.com", "3128675309", emptyList())
        val request = CreateUserRequest(uploadUser, "password", org.id.toString())
        val user = User(name = "Gabe", email = "test@example.com", phone = "3128675309")
        val session = Session(user, "ddd", TimeSource().now())
        every { userService.createUser(any(), any(), any()) } returns user
        every { loginAuthenticator.createSession(any()) } returns session
        val result = userController.createUser(request)
        assertEquals(NetworkErrorType.NONE.toString(), result.errorType)
        assertEquals(user.name, result.data!!.user.name)
        assertEquals(session.token, result.data.session)
    }

    @Test
    fun `getLoggedInUser returns converted user`() {
        val user = User(name = "Gabe", email = "test@example.com", phone = "3128675309")
        val result = userController.getLoggedInUser(user)
        assertEquals(NetworkErrorType.NONE.toString(), result.errorType)
        assertEquals(user.name, result.data!!.name)
    }

    @Test
    fun `updateLoggedInUser returns converted user`() {
        val user = User(name = "Gabe", email = "test@example.com", phone = "3128675309")
        every { userService.updateUser(user = any(), newName = any(), newEmail = any(), newPhone = any(), newPassword = any()) } returns user
        val patchUser = PatchUser(null, null, null, null)
        val request = UpdateUserRequest(patchUser)
        val result = userController.updateLoggedInUser(request, user)
        assertEquals(NetworkErrorType.NONE.toString(), result.errorType)
        assertEquals(user.name, result.data!!.name)
    }
}