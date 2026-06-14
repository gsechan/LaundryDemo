package com.gabesechansoftware.laundrydemoserver.model.dbview.admin

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "admin_role_membership")
class AdminRoleMembership(

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "admin_id", nullable = false)
    var admin: Admin? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    var role: AdminRole? = null,

): BaseEntity()
