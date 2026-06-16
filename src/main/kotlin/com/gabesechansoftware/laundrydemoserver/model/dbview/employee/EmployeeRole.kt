package com.gabesechansoftware.laundrydemoserver.model.dbview.employee

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(
    name = "employee_roles",
    indexes = [Index(name = "idx_employee_roles_organization_id", columnList = "organization_id")],
)
data class EmployeeRole(
    var name: String? = null,

    @Column(name = "organization_id", nullable = false)
    var organizationId: UUID? = null,
) : BaseEntity()
