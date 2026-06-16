package com.gabesechansoftware.laundrydemoserver.model.dbview.orders

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import com.gabesechansoftware.laundrydemoserver.model.dbview.EmbeddedAddress
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
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

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "street1",  column = Column(name = "pickup_street1")),
        AttributeOverride(name = "street2",  column = Column(name = "pickup_street2")),
        AttributeOverride(name = "city",     column = Column(name = "pickup_city")),
        AttributeOverride(name = "state",    column = Column(name = "pickup_state")),
        AttributeOverride(name = "country",  column = Column(name = "pickup_country")),
        AttributeOverride(name = "postcode", column = Column(name = "pickup_postcode")),
    )
    var pickupAddress: EmbeddedAddress? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "street1",  column = Column(name = "dropoff_street1")),
        AttributeOverride(name = "street2",  column = Column(name = "dropoff_street2")),
        AttributeOverride(name = "city",     column = Column(name = "dropoff_city")),
        AttributeOverride(name = "state",    column = Column(name = "dropoff_state")),
        AttributeOverride(name = "country",  column = Column(name = "dropoff_country")),
        AttributeOverride(name = "postcode", column = Column(name = "dropoff_postcode")),
    )
    var dropoffAddress: EmbeddedAddress? = null,
): BaseEntity()