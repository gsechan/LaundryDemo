package com.gabesechansoftware.laundrydemoserver.model.inventory

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "dry_clean_item_names")
class DryCleanItemName(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "item_id", nullable = false)
    var itemId: UUID? = null,
    var name: String? = null,
    var locale: String? = null,
)