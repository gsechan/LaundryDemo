package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories

import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DryCleanItemRepository: JpaRepository<Item,UUID> {
    fun findByOrganization(organization: UUID): List<Item>
    fun findByOrganizationAndId(organization: UUID,itemId: UUID): Item?

}