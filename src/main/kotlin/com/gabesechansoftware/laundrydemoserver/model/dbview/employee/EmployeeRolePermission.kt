package com.gabesechansoftware.laundrydemoserver.model.dbview.employee

import com.gabesechansoftware.laundrydemoserver.authorization.EmployeePermissions
import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(
    name = "employee_role_permissions",
    indexes = [
        Index(name = "idx_employee_role_permissions_role_id", columnList = "role_id"),
        Index(name = "idx_employee_role_permissions_organization_id", columnList = "organization_id"),
    ],
)
class EmployeeRolePermission(

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    var role: EmployeeRole? = null,

    @Column(name = "organization_id", nullable = false)
    var organizationId: UUID? = null,

    @Enumerated(EnumType.STRING)
    var permission: EmployeePermissions? = null,

) : BaseEntity()
