package com.gabesechansoftware.laundrydemoserver.model.dbview

import jakarta.persistence.*


@Entity
@Table(name="organizations")
class Organization(
    var name: String? = null,
    var defaultLocale: String? = null,
): BaseEntity()
