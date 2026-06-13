package com.gabesechansoftware.laundrydemoserver.model.dbview.catalog

import com.gabesechansoftware.laundrydemoserver.Transaltion
import com.gabesechansoftware.laundrydemoserver.TranslationPicker
import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "dry_clean_items")
class DryCleanItem(

    @Column( name = "organization_id", nullable = false)
    @JoinColumn(name = "organization_id", foreignKey = ForeignKey(name = "fk_dry_clean_organization_id"))
    var organization: UUID? = null,

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal? = null,

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    var names: MutableList<DryCleanItemName> = mutableListOf(),

): BaseEntity()

fun getDryCleanItemNameForLocale(
    dryCleanItem: DryCleanItem,
    locale: String,
    translationPicker: TranslationPicker = TranslationPicker()
): String? {
    val locales = listOf(locale)
    val translations = dryCleanItem.names.map { Transaltion(it.name!!, it.locale!!) }
    return translationPicker.findNameMatchingBestLocale(translations, locales)
}
