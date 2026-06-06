package com.gabesechansoftware.laundrydemoserver.model.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

@Entity
@Table(
    indexes = [
        Index(name = "idx_address_user", columnList = "user_id"),
    ]
)
class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "address_seq", sequenceName = "address_id_seq", allocationSize = 1)
    var id: Long = 0

    @Column(nullable = false)
    val street1: String? = null

    @Column
    val street2: String? = null

    @Column(nullable = false)
    val city: String? = null

    @Column(nullable = false)
    val state: String? = null

    @Column(nullable = false)
    val country: String? = null

    @Column(nullable = false)
    val postcode: String? = null


    //Note:  need to put a partial index on this so we have only 1 true per user.
    //Can't do that in Hibernate, so run by hand:  CREATE UNIQUE INDEX address_unique_default_per_user
    //ON address (user_id)
    //WHERE is_default = true;
    @Column(nullable = false)
    val isDefault: Boolean? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null

}