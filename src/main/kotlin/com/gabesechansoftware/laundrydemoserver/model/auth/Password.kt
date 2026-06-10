package com.gabesechansoftware.laundrydemoserver.model.auth

import com.gabesechansoftware.laundrydemoserver.model.BaseEntity
import com.gabesechansoftware.laundrydemoserver.model.user.User
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "passwords")
class Password(

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: User? = null,

    var hash: String? = null,

 ): BaseEntity()