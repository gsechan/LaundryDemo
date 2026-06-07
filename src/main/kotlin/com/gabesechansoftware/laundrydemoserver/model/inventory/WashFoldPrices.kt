package com.gabesechansoftware.laundrydemoserver.model.inventory

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(
    indexes = [
        Index(name = "idx_washfold_org", columnList = "organization_id"),
    ]
)
class WashFoldPrices {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
     var id: UUID? = null

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal? = null

    @Column(nullable = false, precision = 10, scale = 2)
    var avgWeight: BigDecimal? = null

    @Column( name = "organization_id", nullable = false, unique = true)
    @JoinColumn(name = "organization_id", foreignKey = ForeignKey(name = "fk_wash_fold_organization_id"))
    val organization: UUID? = null
}