package com.gabesechansoftware.laundrydemoserver.repositories

import com.gabesechansoftware.laundrydemoserver.model.Organization
import com.gabesechansoftware.laundrydemoserver.model.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrganizationRepository: JpaRepository<Organization, UUID>{

}