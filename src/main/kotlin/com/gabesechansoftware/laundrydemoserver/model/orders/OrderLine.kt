package com.gabesechansoftware.laundrydemoserver.model.orders

import com.gabesechansoftware.laundrydemoserver.model.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.math.BigDecimal
import java.util.UUID

enum class ItemType {
    WASH_AND_FOLD,
    DRY_CLEANING,
    OTHER,
}

@Entity
class OrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column
    var nameInSubmittedLocale: String? = null
    @Column(nullable = false)
    var submittedLocale: String? = null

    @Column
    var nameInOrgLocale: String? = null
    @Column(nullable = false)
    var orgLocale: String? = null

    @Column
    var nameInEnglishLocale: String? = null

    @Column(nullable = false)
    var pricePerUnit: BigDecimal? = null

    @Column  //null on this column means unknown.  Generally due to waiting for measurement
    var quantity: BigDecimal? = null

    @Column  //null on this column means unknown.  Generally due to waiting for measurement
    var totalCost: BigDecimal? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var itemType: ItemType? = null

}