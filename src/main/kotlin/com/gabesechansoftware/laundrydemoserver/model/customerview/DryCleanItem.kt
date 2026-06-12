package com.gabesechansoftware.laundrydemoserver.model.customerview

import com.gabesechansoftware.laundrydemoserver.Transaltion
import com.gabesechansoftware.laundrydemoserver.TranslationPicker
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.DryCleanItem as DBDryCleanItem

data class DryCleanItem(
    val id: String,
    val name: String,
    val price: String
)

fun DBDryCleanItem.toCustomer(locale: String, translationPicker: TranslationPicker = TranslationPicker()): DryCleanItem {
    val locales = listOf(locale, "en-US")
    val translations = names.map { Transaltion(it.name!!, it.locale!!) }
    val name = translationPicker.findNameMatchingBestLocale(translations, locales) ?: "Unknown Item"
    return DryCleanItem(id.toString(), name, price.toString())
}