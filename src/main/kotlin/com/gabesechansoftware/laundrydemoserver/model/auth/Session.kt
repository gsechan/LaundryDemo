package com.gabesechansoftware.laundrydemoserver.model.auth

import com.gabesechansoftware.laundrydemoserver.model.User
import jakarta.persistence.*
import org.hibernate.annotations.TimeZoneStorage
import org.hibernate.annotations.TimeZoneStorageType
import java.time.OffsetDateTime


@Entity
class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "session_seq", sequenceName = "session_id_seq", allocationSize = 1)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null

    @Column(nullable = false)
    var token: String? = null

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(name = "expiration", nullable = false, columnDefinition = "TIMESTAMP(9)")
    var expiration: OffsetDateTime? = null

}