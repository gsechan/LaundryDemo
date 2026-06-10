package com.gabesechansoftware.laundrydemoserver.model.user

import com.gabesechansoftware.laundrydemoserver.model.BaseEntity
import com.gabesechansoftware.laundrydemoserver.model.Organization
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    var name: String? = null,
    var email: String? = null,
    var phone: String? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    var organization: Organization? = null,
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    var addresses: MutableList<Address> = mutableListOf(),
): BaseEntity()