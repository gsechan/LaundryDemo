package com.gabesechansoftware.laundrydemoserver.catalog

import com.gabesechansoftware.laundrydemoserver.Transaltion
import com.gabesechansoftware.laundrydemoserver.TranslationPicker
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.DryCleanItem
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.DryCleanItemRepository
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class DryCleanItemService(
    private val dryCleanItemRepository: DryCleanItemRepository,
    private val translationPicker: TranslationPicker = TranslationPicker(),
) {
     fun getDryCleanItems(org: UUID, locale:String): List<DryCleanItem> {
          return dryCleanItemRepository.findByOrganization(org)

     }

     fun getDryCleanItemNameForLocale(item: DryCleanItem, locale:String): String? {
               val locales = listOf(locale)
               val translations = item.names.map { Transaltion(it.name!!, it.locale!!) }
               return translationPicker.findNameMatchingBestLocale(translations, locales)
     }

     fun getDryCleanItem(org: UUID,item: UUID): DryCleanItem {
          return dryCleanItemRepository.findByOrganizationAndId(org, item)!!
     }

}