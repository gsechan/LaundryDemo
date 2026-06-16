package com.gabesechansoftware.laundrydemoserver.model.dbview

import jakarta.persistence.Embeddable

@Embeddable
class EmbeddedAddress(
    var street1: String = "",
    var street2: String? = null,
    var city: String = "",
    var state: String = "",
    var country: String = "",
    var postcode: String = "",
)
