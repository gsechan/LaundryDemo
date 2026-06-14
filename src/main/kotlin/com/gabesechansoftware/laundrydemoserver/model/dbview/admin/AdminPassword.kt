package com.gabesechansoftware.laundrydemoserver.model.dbview.admin

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "adminpasswords")
class AdminPassword(

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "admin_id", nullable = false, unique = true)
    var admin: Admin? = null,

    var hash: String? = null,

): BaseEntity()
