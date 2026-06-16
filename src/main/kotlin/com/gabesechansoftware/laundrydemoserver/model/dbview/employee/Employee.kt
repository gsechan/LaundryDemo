package com.gabesechansoftware.laundrydemoserver.model.dbview.employee

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(
    name = "employees",
    indexes = [Index(name = "idx_employees_organization_id", columnList = "organization_id")]
)
data class Employee(
    var name: String? = null,
    var email: String? = null,
    var phone: String? = null,
    @Column(name = "organization_id", nullable = false)
    var organizationId: UUID? = null,
) : BaseEntity()
