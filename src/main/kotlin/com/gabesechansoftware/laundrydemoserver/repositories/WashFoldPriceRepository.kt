package com.gabesechansoftware.laundrydemoserver.repositories

import com.gabesechansoftware.laundrydemoserver.model.WashFoldPrices
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

interface WashFoldPriceRepository: JpaRepository<WashFoldPrices, Long>{
    fun findByOrganization(organization: UUID): List<WashFoldPrices>
}