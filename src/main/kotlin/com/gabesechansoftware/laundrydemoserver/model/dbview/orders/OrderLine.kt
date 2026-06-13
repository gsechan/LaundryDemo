package com.gabesechansoftware.laundrydemoserver.model.dbview.orders

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemType
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name="order_lines")
class OrderLine(

    var nameInSubmittedLocale: String? = null,
    var submittedLocale: String? = null,
    var nameInOrgLocale: String? = null,
    var orgLocale: String? = null,
    var nameInEnglishLocale: String? = null,
    var pricePerUnit: BigDecimal? = null,
    var quantity: BigDecimal? = null,  //Null here means unknown quantity (quantity TBD)
    var totalCost: BigDecimal? = null, //Null here means we don't have a quantity yet to calculate this

    @Enumerated(EnumType.STRING)
    var itemType: ItemType? = null,
): BaseEntity()