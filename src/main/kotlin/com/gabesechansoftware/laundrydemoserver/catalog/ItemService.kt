package com.gabesechansoftware.laundrydemoserver.catalog

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemName
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemType
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.ItemRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

data class UploadItemName(
     val name: String,
     val locale: String,
)

data class UploadItem(
     val price: String,
     val itemType: ItemType,
     val names: List<UploadItemName>,
)

data class PatchItem(
     val price: String?,
     val itemType: ItemType?,
)

@Service
class ItemService(
     private val itemRepository: ItemRepository,
) {
     fun getItems(org: UUID): List<Item> {
          return itemRepository.findByOrganization(org)

     }

     fun getItem(org: UUID, item: UUID): Item {
          return itemRepository.findByOrganizationAndId(org, item)?: throw EntityDoesNotExistException("Item does not exist")
     }

     @Transactional
     fun createItem(org: UUID, upload: UploadItem): Item {
          val price = parsePrice(upload.price)
          val item = Item(organization = org, price = price, itemType = upload.itemType)
          // BaseEntity assigns the id at construction, so we can set the FK on the
          // names up front (the join column is NOT NULL).
          upload.names.forEach {
               item.names.add(ItemName(itemId = item.id, name = it.name, locale = it.locale))
          }
          itemRepository.save(item)
          return item
     }

     @Transactional
     fun updateItem(org: UUID, itemId: UUID, patch: PatchItem): Item {
          val item = getItem(org, itemId)
          patch.price?.let { item.price = parsePrice(it) }
          patch.itemType?.let { item.itemType = it }
          itemRepository.save(item)
          return item
     }

     @Transactional
     fun deleteItem(org: UUID, itemId: UUID) {
          val item = getItem(org, itemId)
          itemRepository.delete(item)
     }

     private fun parsePrice(price: String): BigDecimal {
          return try {
               BigDecimal(price)
          } catch (e: NumberFormatException) {
               throw APIErrorException(listOf("Invalid price"))
          }
     }

}
