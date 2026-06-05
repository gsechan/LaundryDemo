package com.gabesechansoftware.laundrydemoserver.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import java.math.BigDecimal

enum class Category {
    DryCleaning, Other
}

@Entity
class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "item_seq", sequenceName = "item_id_seq", allocationSize = 1)
    var id: Long = 0

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: Category? = null


}