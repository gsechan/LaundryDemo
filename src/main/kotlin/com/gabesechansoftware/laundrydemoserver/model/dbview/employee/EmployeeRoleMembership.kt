package com.gabesechansoftware.laundrydemoserver.model.dbview.employee

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(
    name = "employee_role_membership",
    indexes = [
        Index(name = "idx_employee_role_membership_employee_id", columnList = "employee_id"),
        Index(name = "idx_employee_role_membership_role_id", columnList = "role_id"),
        Index(name = "idx_employee_role_membership_location_id", columnList = "location_id"),
    ],
)
class EmployeeRoleMembership(

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    var employee: Employee? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    var role: EmployeeRole? = null,

    // Null means the role applies org-wide; non-null scopes it to a specific location.
    @Column(name = "location_id", nullable = true)
    var locationId: UUID? = null,

) : BaseEntity()
