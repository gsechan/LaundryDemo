package com.gabesechansoftware.laundrydemoserver.catalog

import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.ItemRepository
import org.springframework.stereotype.Service
import java.util.UUID


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

}
