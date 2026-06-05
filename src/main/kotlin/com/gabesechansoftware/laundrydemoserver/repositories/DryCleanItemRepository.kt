package com.gabesechansoftware.laundrydemoserver.repositories

import com.gabesechansoftware.laundrydemoserver.model.DryCleanItem
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DryCleanItemRepository: JpaRepository<DryCleanItem,Long> {
    fun findByOrganization(organization: UUID): List<DryCleanItem>
}