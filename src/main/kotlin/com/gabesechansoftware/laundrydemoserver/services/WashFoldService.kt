package com.gabesechansoftware.laundrydemoserver.services

import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.WashFoldPriceRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

data class WashFoldData(val price: BigDecimal, val avgWeight: BigDecimal)

@Service
@Transactional
class WashFoldService(
    @Autowired private val washFoldPriceRepository: WashFoldPriceRepository,
) {

    fun washFoldPrice(org: UUID): WashFoldData {
        val results = washFoldPriceRepository.findByOrganization(org)
        if(results.size != 1) {
            throw IllegalStateException("There should be exactly one wash price for $org, found ${results.size}")
        }
        return WashFoldData(results[0].price!!, results[0].avgWeight!!)
    }
}