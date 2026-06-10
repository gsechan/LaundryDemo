package com.gabesechansoftware.laundrydemoserver.model.dbview.user

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name="addresses")
class Address(

    var street1: String? = null,
    var street2: String? = null,
    var city: String? = null,
    var state: String? = null,
    var country: String? = null,
    var postcode: String? = null,
    var isDefault: Boolean? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,
): BaseEntity()