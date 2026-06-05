package com.gabesechansoftware.laundrydemoserver.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import java.math.BigDecimal
import java.util.UUID

@Entity
class DryCleanItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null


    @Column( name = "organization_id", nullable = false)
    @JoinColumn(name = "organization_id", foreignKey = ForeignKey(name = "fk_dry_clean_organization_id"))
    var organization: UUID? = null

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal? = null

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    var names: List<DryCleanItemName> = emptyList()
}

