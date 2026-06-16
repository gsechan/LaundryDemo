package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories

import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ItemRepository: JpaRepository<Item, UUID> {
    fun findByLocationId(locationId: UUID): List<Item>
    fun findByLocationIdAndId(locationId: UUID, itemId: UUID): Item?
}