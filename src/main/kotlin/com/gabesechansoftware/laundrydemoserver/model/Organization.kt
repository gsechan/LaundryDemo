package com.gabesechansoftware.laundrydemoserver.model

import jakarta.persistence.*
import java.util.*


@Entity
@Table(name="organizations")
class Organization(
    var name: String? = null,
    var defaultLocale: String? = null,
): BaseEntity()
