package com.gabesechansoftware.laundrydemoserver.repositories

import com.gabesechansoftware.laundrydemoserver.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrganizationRepository: JpaRepository<User, UUID>{

}