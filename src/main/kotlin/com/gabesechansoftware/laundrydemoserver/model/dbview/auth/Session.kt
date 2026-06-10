package com.gabesechansoftware.laundrydemoserver.model.dbview.auth

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import jakarta.persistence.*
import org.hibernate.annotations.TimeZoneStorage
import org.hibernate.annotations.TimeZoneStorageType
import java.time.OffsetDateTime


@Entity
@Table(name="sessions")
class Session(

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    var token: String? = null,

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(name = "expiration", nullable = false, columnDefinition = "TIMESTAMP(9)")
    var expiration: OffsetDateTime? = null,

): BaseEntity()