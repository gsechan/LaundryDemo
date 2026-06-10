package com.gabesechansoftware.laundrydemoserver.model

import jakarta.persistence.*
import java.util.*


@Entity
@Table(name="organizations")
class Organization(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var name: String? = null,
    var defaultLocale: String? = null,
)
