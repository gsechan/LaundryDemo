package com.gabesechansoftware.laundrydemoserver.model.dbview.employee

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.TimeZoneStorage
import org.hibernate.annotations.TimeZoneStorageType
import java.time.OffsetDateTime

@Entity
@Table(name = "employeesessions")
class EmployeeSession(

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    var employee: Employee? = null,

    var token: String? = null,

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(name = "expiration", nullable = false, columnDefinition = "TIMESTAMP(9)")
    var expiration: OffsetDateTime? = null,

) : BaseEntity()
