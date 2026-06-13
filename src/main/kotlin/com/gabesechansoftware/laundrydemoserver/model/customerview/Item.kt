package com.gabesechansoftware.laundrydemoserver.model.customerview

import com.gabesechansoftware.laundrydemoserver.Transaltion
import com.gabesechansoftware.laundrydemoserver.TranslationPicker
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item as DBItem

data class Item(
    val id: String,
    val name: String,
    val price: String,
    val itemType: String,
)

fun DBItem.toCustomer(locale: String, translationPicker: TranslationPicker = TranslationPicker()): Item {
    val locales = listOf(locale, "en-US")
    val translations = names.map { Transaltion(it.name!!, it.locale!!) }
    val name = translationPicker.findNameMatchingBestLocale(translations, locales) ?: "Unknown Item"
    return Item(id.toString(), name, price.toString(), itemType.name)
}
