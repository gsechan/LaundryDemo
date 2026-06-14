package com.gabesechansoftware.laundrydemoserver.model.dbview.admin

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "admin_roles")
data class AdminRole(
    var name: String? = null,
): BaseEntity()
