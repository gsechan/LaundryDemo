package com.gabesechansoftware.laundrydemoserver.model

import jakarta.persistence.*
import java.util.*


@Entity
@Table(name="organizations")
class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    open var id: UUID? = null

    @Column(nullable = false)
    open var name: String? = null

    @Column
    var defaultLocale: String? = null

}
