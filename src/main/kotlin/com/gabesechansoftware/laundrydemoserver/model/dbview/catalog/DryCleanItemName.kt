package com.gabesechansoftware.laundrydemoserver.model.dbview.catalog

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "dry_clean_item_names")
class DryCleanItemName(

    @Column(name = "item_id", nullable = false)
    var itemId: UUID? = null,
    var name: String? = null,
    var locale: String? = null,
): BaseEntity()