package com.gabesechansoftware.laundrydemoserver.model.dbview.admin

import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "admin_role_permissions")
class AdminRolePermission(

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    var role: AdminRole? = null,

    @Enumerated(EnumType.STRING)
    var permission: AdminPermissions? = null,

): BaseEntity()
