package com.gabesechansoftware.laundrydemoserver.model.dbview

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "locations",
    indexes = [
        Index(name = "idx_locations_organization_id", columnList = "organization_id"),
        Index(name = "idx_locations_postcode", columnList = "postcode"),
    ]
)
class Location(
    var name: String = "",
    @Embedded
    var address: EmbeddedAddress = EmbeddedAddress(),
    @Column(name = "organization_id", nullable = false)
    var organizationId: UUID = UUID.randomUUID(),
) : BaseEntity()
