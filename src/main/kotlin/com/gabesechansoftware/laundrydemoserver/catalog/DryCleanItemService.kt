package com.gabesechansoftware.laundrydemoserver.catalog

import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.DryCleanItemRepository
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class DryCleanItemService(
    private val dryCleanItemRepository: DryCleanItemRepository,
) {
     fun getDryCleanItems(org: UUID): List<Item> {
          return dryCleanItemRepository.findByOrganization(org)

     }

     fun getDryCleanItem(org: UUID,item: UUID): Item {
          return dryCleanItemRepository.findByOrganizationAndId(org, item)?: throw EntityDoesNotExistException("Item does not exist")
     }

}
