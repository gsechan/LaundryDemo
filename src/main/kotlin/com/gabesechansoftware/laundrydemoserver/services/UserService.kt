package com.gabesechansoftware.laundrydemoserver.services

import com.gabesechansoftware.laundrydemoserver.model.User
import com.gabesechansoftware.laundrydemoserver.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(@Autowired private val userRepository: UserRepository) {


}