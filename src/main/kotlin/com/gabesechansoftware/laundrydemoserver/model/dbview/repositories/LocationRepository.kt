package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories

import com.gabesechansoftware.laundrydemoserver.model.dbview.Location
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LocationRepository : JpaRepository<Location, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<Location>
    fun findFirstByOrganizationIdAndPostcode(organizationId: UUID, postcode: String): Location?
}
