package com.gabesechansoftware.laundrydemoserver.model.auth

import com.gabesechansoftware.laundrydemoserver.model.user.User
import jakarta.persistence.*
import org.hibernate.annotations.TimeZoneStorage
import org.hibernate.annotations.TimeZoneStorageType
import java.time.OffsetDateTime
import java.util.UUID


@Entity
@Table(name="sessions")
class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null

    @Column(nullable = false)
    var token: String? = null

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(name = "expiration", nullable = false, columnDefinition = "TIMESTAMP(9)")
    var expiration: OffsetDateTime? = null

}