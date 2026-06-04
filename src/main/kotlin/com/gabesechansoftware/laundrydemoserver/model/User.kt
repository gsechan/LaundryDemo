package com.gabesechansoftware.laundrydemoserver.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "user_org_and_phone", columnNames = [ "phone", "organization_id" ])
    ],
    indexes = [
        Index(name = "idx_user_phone", columnList = "phone, organization_id", unique = true),
    ]
)
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    open var id: UUID? = null

    @Column(nullable = false)
    open var name: String? = null

    open var email: String? = null

    @Column(nullable = false)
    open var phone: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    open var organization: Organization? = null

}