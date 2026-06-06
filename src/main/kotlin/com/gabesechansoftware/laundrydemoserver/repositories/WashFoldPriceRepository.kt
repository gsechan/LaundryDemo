package com.gabesechansoftware.laundrydemoserver.repositories

import com.gabesechansoftware.laundrydemoserver.model.inventory.WashFoldPrices
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WashFoldPriceRepository: JpaRepository<WashFoldPrices, Long>{
    fun findByOrganization(organization: UUID): List<WashFoldPrices>
}