package com.gabesechansoftware.laundrydemoserver.catalog

import com.gabesechansoftware.laundrydemoserver.DatabaseDataInvalidException
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.WashFoldPrice
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.WashFoldPriceRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class WashFoldService(
    private val washFoldPriceRepository: WashFoldPriceRepository
) {

    fun washFoldPrice(org: UUID): WashFoldPrice {
        val results = washFoldPriceRepository.findByOrganization(org)
        if(results.size != 1) {
            throw DatabaseDataInvalidException("There should be exactly one wash price for $org, found ${results.size}")
        }
        return results[0]
    }

    fun washFoldPriceInternal(org: UUID): WashFoldPrice {
        val results = washFoldPriceRepository.findByOrganization(org)
        if(results.size != 1) {
            throw DatabaseDataInvalidException("There should be exactly one wash price for $org, found ${results.size}")
        }
        return results.first()
    }

}