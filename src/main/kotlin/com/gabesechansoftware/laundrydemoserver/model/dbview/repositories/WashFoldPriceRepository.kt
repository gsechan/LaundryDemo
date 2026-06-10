package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories

import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.WashFoldPrice
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WashFoldPriceRepository: JpaRepository<WashFoldPrice, UUID>{
    fun findByOrganization(organization: UUID): List<WashFoldPrice>
}