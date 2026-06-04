package com.gabesechansoftware.laundrydemoserver.model.auth

import com.gabesechansoftware.laundrydemoserver.model.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
class Password {
    @Id
    var id: Long? = null

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: User? = null

    @Column(nullable = false)
    var hash: String? = null

}