package com.gabesechansoftware.laundrydemoserver.catalog

import com.gabesechansoftware.laundrydemoserver.DataConstraintException
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.WashFoldPrice
import com.gabesechansoftware.laundrydemoserver.model.customerview.WashFoldPrice as CustomerWashFoldPrice
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.WashFoldPriceRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class WashFoldService(
    private val washFoldPriceRepository: WashFoldPriceRepository
) {

    fun washFoldPriceForCustomer(org: UUID): CustomerWashFoldPrice {
        val results = washFoldPriceRepository.findByOrganization(org)
        if(results.size != 1) {
            throw DataConstraintException("There should be exactly one wash price for $org, found ${results.size}")
        }
        return CustomerWashFoldPrice(results[0].price.toString(), results[0].avgWeight.toString(), "Wash and Fold")
    }

    fun washFoldPriceInternal(org: UUID): WashFoldPrice {
        val results = washFoldPriceRepository.findByOrganization(org)
        if(results.size != 1) {
            throw DataConstraintException("There should be exactly one wash price for $org, found ${results.size}")
        }
        return results.first()
    }

}