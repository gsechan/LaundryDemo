package com.gabesechansoftware.laundrydemoserver.model.dbview.admin

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "admins")
data class Admin(
    var name: String? = null,
    var email: String? = null,
    var phone: String? = null,
): BaseEntity()
