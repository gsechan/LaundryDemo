package com.gabesechansoftware.laundrydemoserver.catalog

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemName
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemType
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.ItemRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.LocationRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
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
     val names: List<UploadItemName>?,
)

@Service
class ItemService(
     private val itemRepository: ItemRepository,
     private val addressRepository: AddressRepository,
     private val locationRepository: LocationRepository,
) {
     fun getItems(locationId: UUID): List<Item> =
          itemRepository.findByLocationId(locationId)

     fun getItemsForUser(user: User, addressId: UUID?): List<Item> {
          val address = if (addressId != null) {
               addressRepository.findById(addressId).orElse(null)
          } else {
               addressRepository.findFirstByUserAndIsDefault(user, true)
          } ?: return emptyList()

          val orgId = user.organization?.id ?: return emptyList()
          val location = locationRepository.findFirstByOrganizationIdAndAddressPostcode(orgId, address.postcode ?: return emptyList())
               ?: return emptyList()

          return itemRepository.findByLocationId(location.id)
     }

     fun getItem(locationId: UUID, itemId: UUID): Item {
          return itemRepository.findByLocationIdAndId(locationId, itemId)
               ?: throw EntityDoesNotExistException("Item does not exist")
     }

     @Transactional
     fun createItem(locationId: UUID, upload: UploadItem): Item {
          val price = parsePrice(upload.price)
          val item = Item(locationId = locationId, price = price, itemType = upload.itemType)
          upload.names.forEach {
               item.names.add(ItemName(itemId = item.id, name = it.name, locale = it.locale))
          }
          itemRepository.save(item)
          return item
     }

     @Transactional
     fun updateItem(locationId: UUID, itemId: UUID, patch: PatchItem): Item {
          val item = getItem(locationId, itemId)
          patch.price?.let { item.price = parsePrice(it) }
          patch.itemType?.let { item.itemType = it }
          patch.names?.let { newNames ->
               item.names.clear()
               newNames.forEach { n -> item.names.add(ItemName(itemId = item.id, name = n.name, locale = n.locale)) }
          }
          itemRepository.save(item)
          return item
     }

     @Transactional
     fun deleteItem(locationId: UUID, itemId: UUID) {
          val item = getItem(locationId, itemId)
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
