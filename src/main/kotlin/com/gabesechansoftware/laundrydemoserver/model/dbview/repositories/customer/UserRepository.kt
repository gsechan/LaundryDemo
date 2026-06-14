package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.customer

import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository: JpaRepository<User, UUID>
