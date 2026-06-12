package com.gabesechansoftware.laundrydemoserver.model.customerview

import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.WashFoldPrice as DBWashFoldPrice

data class WashFoldPrice(val price: String, val avgWeight: String, val name: String)

fun DBWashFoldPrice.toCustomer() : WashFoldPrice {
    return WashFoldPrice(price!!.toString(), avgWeight!!.toString(), "Wash and fold")
}