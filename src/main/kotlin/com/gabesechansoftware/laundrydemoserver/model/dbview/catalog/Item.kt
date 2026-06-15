package com.gabesechansoftware.laundrydemoserver.model.dbview.catalog

import com.gabesechansoftware.laundrydemoserver.Transaltion
import com.gabesechansoftware.laundrydemoserver.TranslationPicker
import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "items")
class Item(

    @Column( name = "organization_id", nullable = false)
    @JoinColumn(name = "organization_id", foreignKey = ForeignKey(name = "fk_items_orangization_id"))
    var organization: UUID? = null,

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal? = null,

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "item_id")
    var names: MutableList<ItemName> = mutableListOf(),

    @Column(name = "item_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var itemType: ItemType = ItemType.DRY_CLEANING,

): BaseEntity()

fun itemNameForLocale(
    item: Item,
    locale: String,
    translationPicker: TranslationPicker = TranslationPicker()
): String? {
    val locales = listOf(locale)
    val translations = item.names.map { Transaltion(it.name!!, it.locale!!) }
    return translationPicker.findNameMatchingBestLocale(translations, locales)
}
