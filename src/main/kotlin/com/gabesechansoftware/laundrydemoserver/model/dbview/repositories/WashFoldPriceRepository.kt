package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories

import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.WashFoldPrices
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WashFoldPriceRepository: JpaRepository<WashFoldPrices, UUID>{
    fun findByOrganization(organization: UUID): List<WashFoldPrices>
}