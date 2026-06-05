package com.gabesechansoftware.laundrydemoserver.services

import com.gabesechansoftware.laundrydemoserver.model.DryCleanItemName
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
               var foundName: DryCleanItemName? = null
               item.names.forEach { name->
                    if(name.locale == locale) {
                         foundName = name
                         return@forEach
                    }
               }
               if(foundName == null) {
                    val subLocale = locale.substringBefore("-")
                    item.names.forEach { name ->
                         if(name.locale == subLocale) {
                              foundName = name
                              return@forEach
                         }
                    }
               }
               if(foundName == null) {
                    item.names.forEach { name ->
                         if(name.locale?.startsWith("en") ?: false ) {
                              foundName = name
                              return@forEach
                         }
                    }
               }
               val name = foundName?.name ?: "Unknown Item"
               DryCleanItemWithName(item.id!!, name, item.price!!)
          }

     }


}