package com.gabesechansoftware.laundrydemoserver.model.dbview.catalog

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name="wash_fold_prices")
class WashFoldPrices(

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal? = null,

    @Column(nullable = false, precision = 10, scale = 2)
    var avgWeight: BigDecimal? = null,

    @Column( name = "organization_id", nullable = false, unique = true)
    @JoinColumn(name = "organization_id", foreignKey = ForeignKey(name = "fk_wash_fold_organization_id"))
    val organization: UUID? = null,
): BaseEntity()