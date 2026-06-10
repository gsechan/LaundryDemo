package com.gabesechansoftware.laundrydemoserver.model.dbview.auth

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "passwords")
class Password(

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: User? = null,

    var hash: String? = null,

 ): BaseEntity()