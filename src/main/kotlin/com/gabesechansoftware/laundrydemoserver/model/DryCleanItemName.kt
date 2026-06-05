package com.gabesechansoftware.laundrydemoserver.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.SequenceGenerator
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