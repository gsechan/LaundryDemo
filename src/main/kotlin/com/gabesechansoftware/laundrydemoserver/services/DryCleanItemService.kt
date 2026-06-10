package com.gabesechansoftware.laundrydemoserver.services

import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.DryCleanItem
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.DryCleanItemName
import com.gabesechansoftware.laundrydemoserver.repositories.DryCleanItemRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

data class DryCleanItemWithName(val id: UUID, val name: String, val price: BigDecimal)

@Service
class DryCleanItemService(
     private val dryCleanItemRepository: DryCleanItemRepository,
) {
     fun getDryCleanItems(org: UUID, locale:String): List<DryCleanItemWithName> {
          return dryCleanItemRepository.findByOrganization(org).map { item->
               var name = findMatchingNameForItem(item.names, locale)
               if(name == null) {
                    name = findMatchingNameForItem(item.names, "en-US")
               }
               if(name == null) {
                    name = "Unkown Item"
               }
               DryCleanItemWithName(item.id!!, name, item.price!!)
          }

     }

     fun getDryCleanItem(org: UUID,item: UUID): DryCleanItem {
          return dryCleanItemRepository.findByOrganizationAndId(org, item)!!
     }

     fun findMatchingNameForItem(names: List<DryCleanItemName>, locale: String): String? {
          names.forEach { name->
               if(name.locale == locale) {
                    return@findMatchingNameForItem name.name
               }
          }
           val subLocale = locale.substringBefore("-")
           names.forEach { name ->
               if(name.locale == subLocale) {
                    return@findMatchingNameForItem name.name
               }
          }
          return null
     }
}