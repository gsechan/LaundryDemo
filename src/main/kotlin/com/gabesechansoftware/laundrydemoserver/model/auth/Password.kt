package com.gabesechansoftware.laundrydemoserver.model.auth

import com.gabesechansoftware.laundrydemoserver.model.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

@Entity
@Table(
    indexes = [
        Index(name = "idx_password_user", columnList = "user_id", unique = true),
    ]
)
class Password {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "password_seq", sequenceName = "password_id_seq", allocationSize = 1)
    var id: Long? = null

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: User? = null

    @Column(nullable = false)
    var hash: String? = null

}