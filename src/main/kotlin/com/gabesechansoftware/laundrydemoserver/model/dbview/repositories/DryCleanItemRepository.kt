package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories

import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.DryCleanItem
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DryCleanItemRepository: JpaRepository<DryCleanItem,UUID> {
    fun findByOrganization(organization: UUID): List<DryCleanItem>
    fun findByOrganizationAndId(organization: UUID,itemId: UUID): DryCleanItem?

}