package com.gabesechansoftware.laundrydemoserver.repositories

import com.gabesechansoftware.laundrydemoserver.model.Organization
import com.gabesechansoftware.laundrydemoserver.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository: JpaRepository<Organization, UUID>{

}