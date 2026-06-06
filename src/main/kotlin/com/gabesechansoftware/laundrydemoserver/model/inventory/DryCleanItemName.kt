package com.gabesechansoftware.laundrydemoserver.model.inventory

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.UUID

@Entity
class DryCleanItemName {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "item_id", nullable = false)
    var itemId: UUID? = null

    @Column(nullable = false)
    var name: String? = null

    @Column(nullable = false)
    var locale: String? = null

}