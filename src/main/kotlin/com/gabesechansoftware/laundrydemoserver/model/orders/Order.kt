package com.gabesechansoftware.laundrydemoserver.model.orders

import com.gabesechansoftware.laundrydemoserver.model.user.Address
import com.gabesechansoftware.laundrydemoserver.model.user.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.TimeZoneStorage
import org.hibernate.annotations.TimeZoneStorageType
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

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
class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var state: OrderState? = null

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(name = "order_id")
    open var lines: Set<OrderLine>? = null

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(nullable = false, columnDefinition = "TIMESTAMP(9)")
    var submitted: OffsetDateTime? = null

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(nullable = false, columnDefinition = "TIMESTAMP(9)")
    var lastChange: OffsetDateTime? = null

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(columnDefinition = "TIMESTAMP(9)")
    var completed: OffsetDateTime? = null

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(columnDefinition = "TIMESTAMP(9)")
    var scheduledPickup: OffsetDateTime? = null

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(columnDefinition = "TIMESTAMP(9)")
    var scheduledDropoff: OffsetDateTime? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dropoff_address_id", nullable = false)
    var dropoff_address: Address? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_address_id", nullable = false)
    var pickup_address: Address? = null

}