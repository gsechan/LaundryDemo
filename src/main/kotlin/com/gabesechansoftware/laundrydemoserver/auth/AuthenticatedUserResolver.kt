package com.gabesechansoftware.laundrydemoserver.auth

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.ObjectMapper

@Component
class AuthenticatedUserResolver(
    private val loginAuthenticator: LoginAuthenticator,
    private val objectMapper: ObjectMapper
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(AuthenticatedUser::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): User {
        val token = webRequest.getHeader("Authorization")
            ?.removePrefix("Bearer ")

        if(token== null) {
            val response = webRequest.getNativeResponse(HttpServletResponse::class.java)!!
            response.status = 200
            response.contentType = "application/json"
            response.writer.write(
                objectMapper.writeValueAsString(
                    NetworkResponse<Unit>(NetworkErrorType.BAD_AUTH, "No token sent")
                )
            )
            response.writer.flush()
            mavContainer?.isRequestHandled = true  // tells Spring the request is fully handled
            throw BadAuthTokenException(token)
        }

        return loginAuthenticator.authenticateToken(token)
    }
}