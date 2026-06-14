package com.gabesechansoftware.laundrydemoserver.model.dbview.orders

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.TimeZoneStorage
import org.hibernate.annotations.TimeZoneStorageType
import java.time.OffsetDateTime

enum class OrderState {
    SUBMITTED,
    PICKUP_IN_PROGRESS,
    PICKED_UP,
    CLEANING,
    AWAITING_DROP_OFF,
    DROPPING_OFF,
    COMPLETED
}

@Entity
@Table(name = "orders")
class Order(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @Enumerated(EnumType.STRING)
    var state: OrderState? = null,

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(name = "order_id")
    var lines: MutableList<OrderLine> = mutableListOf(),

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(columnDefinition = "TIMESTAMP(9)")
    var submitted: OffsetDateTime? = null,

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(columnDefinition = "TIMESTAMP(9)")
    var lastChange: OffsetDateTime? = null,

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(columnDefinition = "TIMESTAMP(9)")
    var completed: OffsetDateTime? = null,

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(columnDefinition = "TIMESTAMP(9)")
    var scheduledPickup: OffsetDateTime? = null,

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(columnDefinition = "TIMESTAMP(9)")
    var scheduledDropoff: OffsetDateTime? = null,

    var pickupStreet1: String? = null,
    var pickupStreet2: String? = null,
    var pickupCity: String? = null,
    var pickupState: String? = null,
    var pickupCountry: String? = null,
    var pickupPostcode: String? = null,

    var dropoffStreet1: String? = null,
    var dropoffStreet2: String? = null,
    var dropoffCity: String? = null,
    var dropoffState: String? = null,
    var dropoffCountry: String? = null,
    var dropoffPostcode: String? = null,
): BaseEntity()