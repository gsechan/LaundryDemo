package com.gabesechansoftware.laundrydemoserver.catalog

import com.gabesechansoftware.laundrydemoserver.Transaltion
import com.gabesechansoftware.laundrydemoserver.findNameMatchingBestLocale
import com.gabesechansoftware.laundrydemoserver.model.customerview.DryCleanItem as CustomerDryCleanItem
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.DryCleanItem
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.DryCleanItemRepository
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class DryCleanItemService(
     private val dryCleanItemRepository: DryCleanItemRepository,
) {
     fun getCustomerDryCleanItems(org: UUID, locale:String): List<CustomerDryCleanItem> {
          return dryCleanItemRepository.findByOrganization(org).map { item->
               val locales = listOf(locale, "en-US")
               val translations = item.names.map { Transaltion(it.name!!, it.locale!!) }
               val name = findNameMatchingBestLocale(translations, locales) ?: "Unknown Item"
               CustomerDryCleanItem(item.id.toString(), name, item.price.toString())
          }

     }

     fun getDryCleanItemNameForLocale(item: DryCleanItem, locale:String): String? {
               val locales = listOf(locale)
               val translations = item.names.map { Transaltion(it.name!!, it.locale!!) }
               return findNameMatchingBestLocale(translations, locales)
     }

     fun getDryCleanItem(org: UUID,item: UUID): DryCleanItem {
          return dryCleanItemRepository.findByOrganizationAndId(org, item)!!
     }

}